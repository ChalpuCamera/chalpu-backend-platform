package com.example.chalpuplatform.fooditem.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.FoodException;
import com.example.chalpuplatform.fooditem.domain.FoodCategory;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.fooditem.domain.MenuExtractionProgress;
import com.example.chalpuplatform.fooditem.dto.ExtractedFoodItemDto;
import com.example.chalpuplatform.fooditem.repository.FoodItemRepository;
import com.example.chalpuplatform.fooditem.repository.MenuExtractionProgressRepository;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuPersistenceService {

    private final FoodItemRepository foodItemRepository;
    private final MenuExtractionProgressRepository progressRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public void saveMenuItemsWithProgress(Long storeId, List<ExtractedFoodItemDto> items,
                                         String requestId) {
        log.info("Thread [{}]: DB 트랜잭션 시작 - Store ID: {}, Items: {}개",
                Thread.currentThread().getName(), storeId, items.size());

        // Progress 조회
        MenuExtractionProgress progress = progressRepository.findByRequestId(requestId)
                .orElseThrow(() -> new FoodException(ErrorMessage.EXTRACTION_PROGRESS_NOT_FOUND));

        // Store 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new FoodException(ErrorMessage.STORE_NOT_FOUND));

        // 메뉴 아이템들 저장
        int addedCount = saveFoodItems(store, items);

        // 진행률 완료 처리
        progress.complete(addedCount);
        progressRepository.save(progress);

        log.info("Thread [{}]: DB 트랜잭션 완료 - {}개 아이템 추가",
                Thread.currentThread().getName(), addedCount);
    }

    @Transactional
    public void handleExtractionFailure(String requestId, Throwable error) {
        log.error("Thread [{}]: 추출 실패 처리 - Request ID: {}, Error: {}",
                Thread.currentThread().getName(), requestId, error.getMessage());

        MenuExtractionProgress progress = progressRepository.findByRequestId(requestId)
                .orElseThrow(() -> new FoodException(ErrorMessage.EXTRACTION_PROGRESS_NOT_FOUND));

        progress.fail(error.getMessage());
        progressRepository.save(progress);
    }

    private int saveFoodItems(Store store, List<ExtractedFoodItemDto> extractedItems) {
        int addedCount = 0;
        List<FoodItem> itemsToSave = new ArrayList<>();

        for (ExtractedFoodItemDto item : extractedItems) {
            // 기존 아이템 확인 (이름으로 중복 체크)
            boolean exists = foodItemRepository.findByStoreIdAndFoodName(
                    store.getId(), item.getName()).isPresent();

            if (!exists) {
                FoodItem foodItem = FoodItem.builder()
                        .store(store)
                        .foodName(item.getName())
                        .price(BigDecimal.valueOf(item.getPrice()))
                        .description(item.getDescription())
                        .category(item.getCategory())
                        .isActive(true)
                        .build();

                itemsToSave.add(foodItem);
                addedCount++;

                log.debug("Thread [{}]: 메뉴 아이템 추가 예정 - Store ID: {}, 메뉴명: {}, 카테고리: {}",
                        Thread.currentThread().getName(), store.getId(), item.getName(), item.getCategory());
            } else {
                log.debug("Thread [{}]: 메뉴 아이템 이미 존재 - Store ID: {}, 메뉴명: {}",
                        Thread.currentThread().getName(), store.getId(), item.getName());
            }
        }

        if (!itemsToSave.isEmpty()) {
            foodItemRepository.saveAll(itemsToSave);
            log.info("Thread [{}]: {}개 메뉴 아이템 저장 완료",
                    Thread.currentThread().getName(), itemsToSave.size());
        }

        return addedCount;
    }
}