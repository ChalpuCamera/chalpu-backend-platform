package com.example.chalpuplatform.fooditem.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.FoodException;
import com.example.chalpuplatform.fooditem.domain.FoodCategory;
import com.example.chalpuplatform.fooditem.domain.MenuExtractionProgress;
import com.example.chalpuplatform.fooditem.dto.ExtractedFoodItemDto;
import com.example.chalpuplatform.fooditem.dto.FoodItemExtractionStatusResponse;
import com.example.chalpuplatform.fooditem.repository.MenuExtractionProgressRepository;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.repository.StoreRepository;
import com.example.chalpuplatform.store.service.UserStoreRoleService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FoodItemExtractionService {

    private final MenuExtractionProgressRepository progressRepository;
    private final StoreRepository storeRepository;
    private final UserStoreRoleService userStoreRoleService;
    private final GeminiService geminiService;
    private final MenuPersistenceService menuPersistenceService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_FOOD_NAME_LENGTH = 30;

    @Transactional
    public String startMenuExtraction(Long userId, Long storeId, MultipartFile image) {
        log.info("Thread [{}]: 메뉴 추출 시작 - Store ID: {}, User ID: {}",
                Thread.currentThread().getName(), storeId, userId);

        // 권한 체크
        if (!userStoreRoleService.canUserManageStore(userId, storeId)) {
            throw new FoodException(ErrorMessage.STORE_ACCESS_DENIED);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new FoodException(ErrorMessage.STORE_NOT_FOUND));

        // 이미 진행 중인 추출이 있는지 확인
        long processingCount = progressRepository.countProcessingByStoreId(storeId);
        if (processingCount > 0) {
            throw new FoodException(ErrorMessage.MENU_EXTRACTION_ALREADY_IN_PROGRESS);
        }

        // 추출 진행 상태 초기화
        String requestId = UUID.randomUUID().toString();
        MenuExtractionProgress progress = MenuExtractionProgress.builder()
                .requestId(requestId)
                .storeId(storeId)
                .status(MenuExtractionProgress.ExtractionStatus.PROCESSING)
                .progressPercentage(10)
                .currentStep("메뉴 추출 준비 중")
                .build();
        progressRepository.saveAndFlush(progress);

        log.info("Thread [{}]: Progress 초기화 완료 - Request ID: {}",
                Thread.currentThread().getName(), requestId);

        try {
            byte[] imageBytes = image.getBytes();
            String mimeType = image.getContentType();

            // Progress 업데이트
            progress.updateProgress(20, "AI 모델로 메뉴 텍스트 추출 중");
            progressRepository.save(progress);

            // WebClient 논블로킹 처리 - 즉시 반환
            geminiService.extractTextFromImage(imageBytes, mimeType)
                    .doOnNext(text -> {
                        log.info("Thread [{}]: 텍스트 파싱 시작",
                                Thread.currentThread().getName());
                        progress.updateProgress(70, "추출된 텍스트 분석 중");
                        progressRepository.save(progress);
                    })
                    .map(this::parseExtractedText)
                    .subscribe(
                            items -> {
                                log.info("Thread [{}]: DB 저장 요청 - {} 개 아이템",
                                        Thread.currentThread().getName(), items.size());
                                progress.updateProgress(85, "데이터베이스에 저장 중");
                                progressRepository.save(progress);

                                // 별도 트랜잭션 서비스에서 저장 처리
                                menuPersistenceService.saveMenuItemsWithProgress(
                                        storeId, items, requestId
                                );
                            },
                            error -> {
                                log.error("Thread [{}]: 메뉴 추출 실패 - {}",
                                        Thread.currentThread().getName(), error.getMessage(), error);
                                // 별도 트랜잭션 서비스에서 실패 처리
                                menuPersistenceService.handleExtractionFailure(
                                        requestId, error
                                );
                            }
                    );

        } catch (IOException e) {
            log.error("Thread [{}]: 이미지 파일 읽기 실패", Thread.currentThread().getName(), e);
            progress.fail("이미지 파일 처리 실패");
            progressRepository.save(progress);
            throw new FoodException(ErrorMessage.FILE_UPLOAD_FAILED);
        }

        log.info("Thread [{}]: 즉시 반환 - Request ID: {}",
                Thread.currentThread().getName(), requestId);
        return requestId;
    }

    public FoodItemExtractionStatusResponse getExtractionStatus(Long userId, String requestId) {
        MenuExtractionProgress progress = progressRepository.findByRequestId(requestId)
                .orElseThrow(() -> new FoodException(ErrorMessage.EXTRACTION_PROGRESS_NOT_FOUND));

        // 권한 체크
        if (!userStoreRoleService.canUserManageStore(userId, progress.getStoreId())) {
            throw new FoodException(ErrorMessage.STORE_ACCESS_DENIED);
        }

        return FoodItemExtractionStatusResponse.builder()
                .requestId(progress.getRequestId())
                .status(progress.getStatus().name())
                .progressPercentage(progress.getProgressPercentage())
                .currentStep(progress.getCurrentStep())
                .errorMessage(progress.getErrorMessage())
                .startedAt(progress.getCreatedAt())  // BaseTimeEntity의 createdAt 사용
                .completedAt(progress.getStatus() == MenuExtractionProgress.ExtractionStatus.COMPLETED
                        || progress.getStatus() == MenuExtractionProgress.ExtractionStatus.FAILED
                        ? progress.getUpdatedAt() : null)  // 완료/실패 시 updatedAt 사용
                .build();
    }

    private List<ExtractedFoodItemDto> parseExtractedText(String extractedText) {
        try {
            Map<String, Object> response = objectMapper.readValue(extractedText, new TypeReference<>() {});
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

            return items.stream()
                    .filter(this::isValidFoodName)
                    .map(item -> {
                        Number priceValue = (Number) item.get("price");
                        return ExtractedFoodItemDto.builder()
                                .name((String) item.get("name"))
                                .price(priceValue != null ? priceValue.intValue() : 0)
                                .description((String) item.get("description"))
                                .category(mapToFoodCategory((String) item.get("category")))
                                .build();
                    })
                    .toList();

        } catch (Exception e) {
            log.error("Thread [{}]: 메뉴 파싱 실패 - {}",
                    Thread.currentThread().getName(), e.getMessage(), e);
            throw new FoodException(ErrorMessage.MENU_PARSING_FAILED);
        }
    }

    private boolean isValidFoodName(Map<String, Object> item) {
        String foodName = (String) item.get("name");
        return foodName != null && foodName.length() <= MAX_FOOD_NAME_LENGTH;
    }

    private FoodCategory mapToFoodCategory(String categoryStr) {
        if (categoryStr == null) return FoodCategory.ETC;

        return switch (categoryStr.toLowerCase()) {
            case "메인요리", "메인", "main" -> FoodCategory.MAIN;
            case "사이드", "side" -> FoodCategory.SIDE;
            case "음료", "drink", "beverage" -> FoodCategory.DRINK;
            case "디저트", "dessert" -> FoodCategory.DESSERT;
            case "전채", "전채요리", "appetizer" -> FoodCategory.APPETIZER;
            case "스프", "soup" -> FoodCategory.SOUP;
            case "샐러드", "salad" -> FoodCategory.SALAD;
            default -> FoodCategory.ETC;
        };
    }

    @Transactional
    public void cleanupTimeoutTasks() {
        // 30분 이상 처리 중인 작업을 타임아웃으로 처리
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(30);
        List<MenuExtractionProgress> timeoutTasks = progressRepository.findTimeoutProcessing(timeout);

        for (MenuExtractionProgress task : timeoutTasks) {
            task.fail("작업 시간 초과");
            progressRepository.save(task);
            log.info("Thread [{}]: 추출 타임아웃 - Request ID: {}",
                    Thread.currentThread().getName(), task.getRequestId());
        }
    }
}