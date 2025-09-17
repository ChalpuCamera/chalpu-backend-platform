package com.example.chalpuplatform.customerfeedback.service;

import com.example.chalpuplatform.customerfeedback.domain.CustomerFeedback;
import com.example.chalpuplatform.customerfeedback.domain.FeedbackPhoto;
import com.example.chalpuplatform.customerfeedback.dto.*;
import com.example.chalpuplatform.customerfeedback.repository.CustomerFeedbackRepository;
import com.example.chalpuplatform.customerfeedback.repository.FeedbackPhotoRepository;

import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.user.domain.UserProfile;
import com.example.chalpuplatform.user.repository.UserRepository;

import com.example.chalpuplatform.survey.domain.Survey;
import com.example.chalpuplatform.survey.domain.SurveyAnswer;
import com.example.chalpuplatform.survey.domain.SurveyQuestion;
import com.example.chalpuplatform.survey.repository.SurveyAnswerRepository;
import com.example.chalpuplatform.survey.repository.SurveyRepository;
import com.example.chalpuplatform.survey.repository.SurveyQuestionRepository;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.UserException;
import com.example.chalpuplatform.common.exception.FeedbackException;
import com.example.chalpuplatform.common.exception.SurveyException;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.fooditem.repository.FoodItemRepository;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.repository.StoreRepository;
import com.example.chalpuplatform.store.service.UserStoreRoleService;
import com.example.chalpuplatform.user.domain.CustomerTaste;
import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomerFeedbackService {

    private final CustomerFeedbackRepository feedbackRepository;
    private final FeedbackPhotoRepository photoRepository;
    private final SurveyAnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final FoodItemRepository foodItemRepository;
    private final StoreRepository storeRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository questionRepository;
    private final S3Presigner s3Presigner;
    private final UserStoreRoleService userStoreRoleService;
    
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public CustomerFeedbackResponse createFeedback(UserDetailsImpl userDetails, FeedbackCreateRequest request) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        FoodItem foodItem = foodItemRepository.findById(request.getFoodId())
                .orElseThrow(() -> new FeedbackException(ErrorMessage.FOODITEM_NOT_FOUND));

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new FeedbackException(ErrorMessage.STORE_NOT_FOUND));

        Survey survey = surveyRepository.findById(request.getSurveyId())
                .orElseThrow(() -> new SurveyException(ErrorMessage.SURVEY_NOT_FOUND));

        CustomerFeedback feedback = CustomerFeedback.createFeedback(foodItem, store, user, survey);
        feedback.setIsViewed(false); // 새 피드백은 읽지 않은 상태로 설정

        // 현재 고객 입맛을 스냅샷으로 저장
        if (user.getUserProfile() != null && user.getUserProfile().getCustomerTaste() != null) {
            CustomerTaste taste = user.getUserProfile().getCustomerTaste();
            feedback.setSpicyLevelSnapshot(taste.getSpicyLevel());
            feedback.setMealAmountSnapshot(taste.getMealAmount());
            feedback.setMealSpendingSnapshot(taste.getMealSpending());
        }

        CustomerFeedback savedFeedback = feedbackRepository.save(feedback);

        saveSurveyAnswers(savedFeedback, request.getSurveyAnswers());

        if (request.getPhotoS3Keys() != null && !request.getPhotoS3Keys().isEmpty()) {
            saveFeedbackPhotos(savedFeedback, request.getPhotoS3Keys());
        }

        if (user.getUserProfile() != null) {
            user.getUserProfile().incrementFeedbackCount();
            user.getUserProfile().incrementRewardCount();
        }

        log.info("고객 피드백 생성 완료: feedbackId={}, userId={}, foodId={}, reward_count_earned=1",
                savedFeedback.getId(), userDetails.getId(), request.getFoodId());

        return mapToCustomerFeedbackResponse(savedFeedback);
    }

    private void saveSurveyAnswers(CustomerFeedback feedback, List<SurveyAnswerRequest> answerRequests) {
        // 모든 질문 ID를 추출
        Set<Long> questionIds = answerRequests.stream()
                .map(SurveyAnswerRequest::getQuestionId)
                .collect(Collectors.toSet());
        
        // 한 번에 모든 질문을 조회하여 Map으로 관리
        Map<Long, SurveyQuestion> questionMap = questionRepository.findAllById(questionIds)
                .stream()
                .collect(Collectors.toMap(SurveyQuestion::getId, Function.identity()));
        
        // 존재하지 않는 질문 ID 검증
        Set<Long> notFoundQuestionIds = questionIds.stream()
                .filter(id -> !questionMap.containsKey(id))
                .collect(Collectors.toSet());
        
        if (!notFoundQuestionIds.isEmpty()) {
            throw new SurveyException(ErrorMessage.SURVEY_QUESTION_NOT_FOUND);
        }
        
        List<SurveyAnswer> answers = answerRequests.stream()
                .map(request -> {
                    SurveyQuestion question = questionMap.get(request.getQuestionId());
                    
                    SurveyAnswer answer = SurveyAnswer.createAnswer(feedback, question, 
                            request.getAnswerText(), request.getNumericValue());
                    
                //     // 답변 유효성 검증
                //     if (!answer.isValidAnswer()) {
                //         throw new SurveyException(ErrorMessage.SURVEY_ANSWER_INVALID);
                //     }
                    
                    return answer;
                })
                .collect(Collectors.toList());

        answerRepository.saveAll(answers);
    }

    private void saveFeedbackPhotos(CustomerFeedback feedback, List<String> photoS3Keys) {
        List<FeedbackPhoto> photos = photoS3Keys.stream()
                .map(s3Key -> {
                    String fileName = extractFileNameFromS3Key(s3Key);
                    return FeedbackPhoto.createFeedbackPhoto(feedback, s3Key, fileName);
                })
                .collect(Collectors.toList());

        photoRepository.saveAll(photos);
    }

    private String extractFileNameFromS3Key(String s3Key) {
        return s3Key.substring(s3Key.lastIndexOf("/") + 1);
    }

    @Transactional(readOnly = true)
    public List<CustomerFeedbackResponse> getUserFeedbacks(UserDetailsImpl userDetails) {
        List<CustomerFeedback> feedbacks = feedbackRepository
                .findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userDetails.getId());

        return feedbacks.stream()
                .map(this::mapToCustomerFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CustomerFeedbackResponse> getUserFeedbacks(UserDetailsImpl userDetails, Pageable pageable) {
        Page<CustomerFeedback> feedbacks = feedbackRepository
                .findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userDetails.getId(), pageable);

        return feedbacks.map(this::mapToCustomerFeedbackResponse);
    }

    @Transactional(readOnly = true)
    public Page<OwnerFeedbackResponse> getStoreFeedbacks(Long storeId, UserDetailsImpl userDetails, Pageable pageable) {
        // 사장님 권한 확인
        if (!userStoreRoleService.canUserManageStore(userDetails.getId(), storeId)) {
            throw new FeedbackException(ErrorMessage.STORE_ACCESS_DENIED);
        }

        Page<CustomerFeedback> feedbacks = feedbackRepository
                .findByStoreIdAndIsActiveTrueOrderByCreatedAtDesc(storeId, pageable);

        return feedbacks.map(this::mapToOwnerFeedbackResponse);
    }

    @Transactional(readOnly = true)
    public CustomerFeedbackResponse getFeedbackById(Long feedbackId) {
        CustomerFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new FeedbackException(ErrorMessage.FEEDBACK_NOT_FOUND));

        return mapToCustomerFeedbackResponse(feedback);
    }


    // 고객용 응답 매핑 (고객 정보 제외)
    private CustomerFeedbackResponse mapToCustomerFeedbackResponse(CustomerFeedback feedback) {
        List<SurveyAnswer> answers = answerRepository.findByFeedbackIdOrderByQuestionId(feedback.getId());
        List<FeedbackPhoto> photos = photoRepository.findByFeedbackIdOrderByCreatedAtAsc(feedback.getId());

        return CustomerFeedbackResponse.builder()
                .feedbackId(feedback.getId())
                .foodName(feedback.getFoodItem().getFoodName())
                .storeName(feedback.getStore().getStoreName())
                .surveyName(feedback.getSurvey().getSurveyName())
                .createdAt(feedback.getCreatedAt())
                .surveyAnswers(answers.stream()
                        .map(SurveyAnswerResponse::from)
                        .collect(Collectors.toList()))
                .photoUrls(photos.stream()
                        .map(FeedbackPhoto::getImageUrl)
                        .collect(Collectors.toList()))
                .build();
    }

    // 사장님용 응답 매핑 (고객 정보 포함)
    private OwnerFeedbackResponse mapToOwnerFeedbackResponse(CustomerFeedback feedback) {
        List<SurveyAnswer> answers = answerRepository.findByFeedbackIdOrderByQuestionId(feedback.getId());
        List<FeedbackPhoto> photos = photoRepository.findByFeedbackIdOrderByCreatedAtAsc(feedback.getId());

        // 9번 질문 답변 찾기 (사장님께 한마디)
        String ownerMessage = answers.stream()
                .filter(answer -> answer.getQuestion().getId().equals(9L))
                .findFirst()
                .map(SurveyAnswer::getAnswerText)
                .orElse(null);

        OwnerFeedbackResponse response = OwnerFeedbackResponse.from(feedback);
        response.setOwnerMessage(ownerMessage);
        response.setPhotoUrls(photos.stream()
                .map(FeedbackPhoto::getImageUrl)
                .collect(Collectors.toList()));

        return response;
    }


    private String createFeedbackPhotoS3Key(final String fileName) {
        Objects.requireNonNull(fileName, "fileName must not be null");
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new FeedbackException(ErrorMessage.PHOTO_INVALID_FORMAT);
        }
        final String fileExtension = fileName.substring(lastDotIndex);
        return "feedback-photos/" + UUID.randomUUID() + fileExtension;
    }

    private URL createPresignedUrl(final String s3Key) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // The URL will be valid for 10 minutes
                .putObjectRequest(objectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url();
    }

    // 다건 읽음 처리 메서드 추가
    @Transactional
    public void markFeedbacksAsViewed(List<Long> feedbackIds, UserDetailsImpl userDetails) {
        List<CustomerFeedback> feedbacks = feedbackRepository.findAllById(feedbackIds);

        for (CustomerFeedback feedback : feedbacks) {
            // 사장님 권한 확인
            if (!userStoreRoleService.canUserManageStore(userDetails.getId(), feedback.getStore().getId())) {
                log.warn("권한 없는 피드백 읽음 처리 시도: feedbackId={}, userId={}",
                    feedback.getId(), userDetails.getId());
                continue;
            }

            feedback.markAsViewed();
        }

        feedbackRepository.saveAll(feedbacks);
        log.info("피드백 다건 읽음 처리: count={}, userId={}", feedbackIds.size(), userDetails.getId());
    }

    public FeedbackPhotosPresignedUrlResponse generateMultipleFeedbackPhotosPresignedUrl(final UserDetailsImpl userDetails, final FeedbackPhotosUploadRequest request) {
        try {
            List<FeedbackPhotosPresignedUrlResponse.FeedbackPhotoUrlInfo> photoUrls = request.getFileNames().stream()
                    .map(fileName -> {
                        try {
                            String s3Key = createFeedbackPhotoS3Key(fileName);
                            URL presignedUrl = createPresignedUrl(s3Key);

                            return FeedbackPhotosPresignedUrlResponse.FeedbackPhotoUrlInfo.builder()
                                    .originalFileName(fileName)
                                    .presignedUrl(presignedUrl.toString())
                                    .s3Key(s3Key)
                                    .build();
                        } catch (Exception e) {
                            log.error("event=single_feedback_photo_presigned_url_generation_failed, user_id={}, file_name={}, error_message={}",
                                    userDetails.getId(), fileName, e.getMessage(), e);
                            throw new FeedbackException(ErrorMessage.PRESIGNED_URL_GENERATION_FAILED);
                        }
                    })
                    .collect(Collectors.toList());

            log.info("event=multiple_feedback_photos_presigned_urls_generated, user_id={}, file_count={}",
                    userDetails.getId(), request.getFileNames().size());

            return FeedbackPhotosPresignedUrlResponse.builder()
                    .photoUrls(photoUrls)
                    .build();
        } catch (Exception e) {
            log.error("event=multiple_feedback_photos_presigned_urls_generation_failed, user_id={}, file_count={}, error_message={}",
                    userDetails.getId(), request.getFileNames() != null ? request.getFileNames().size() : 0, e.getMessage(), e);
            throw new FeedbackException(ErrorMessage.PRESIGNED_URL_GENERATION_FAILED);
        }
    }

    public Page<OwnerFeedbackResponse> getFoodFeedbacks(Long foodId, UserDetailsImpl userDetails, Pageable pageable) {
        // 음식이 속한 매장 조회
        FoodItem foodItem = foodItemRepository.findById(foodId)
                .orElseThrow(() -> new FeedbackException(ErrorMessage.FOODITEM_NOT_FOUND));

        // 사장님 권한 확인
        if (!userStoreRoleService.canUserManageStore(userDetails.getId(), foodItem.getStore().getId())) {
            throw new FeedbackException(ErrorMessage.STORE_ACCESS_DENIED);
        }

        Page<CustomerFeedback> feedbacks = feedbackRepository
                .findByFoodItemIdAndIsActiveTrueOrderByCreatedAtDesc(foodId, pageable);

        return feedbacks.map(this::mapToOwnerFeedbackResponse);
    }


    // 매장의 음식별 읽지 않은 피드백 개수 조회
    @Transactional(readOnly = true)
    public List<FeedbackUnreadCountResponse> getUnreadFeedbackCountsByStore(Long storeId, UserDetailsImpl userDetails) {
        // 사장님 권한 확인
        if (!userStoreRoleService.canUserManageStore(userDetails.getId(), storeId)) {
            throw new FeedbackException(ErrorMessage.STORE_ACCESS_DENIED);
        }

        List<Object[]> results = feedbackRepository.findUnreadCountsByStoreId(storeId);

        return results.stream()
                .map(result -> FeedbackUnreadCountResponse.builder()
                        .foodItemId((Long) result[0])
                        .foodName((String) result[1])
                        .unreadCount((Long) result[2])
                        .totalCount((Long) result[3])
                        .build())
                .collect(Collectors.toList());
    }

    // 고객 입맛 프로필 조회 (피드백 상세에서)
    @Transactional(readOnly = true)
    public OwnerFeedbackResponse getFeedbackWithCustomerTaste(Long feedbackId, UserDetailsImpl userDetails) {
        CustomerFeedback feedback = feedbackRepository.findByIdWithUserProfile(feedbackId);

        if (feedback == null) {
            throw new FeedbackException(ErrorMessage.FEEDBACK_NOT_FOUND);
        }

        // 사장님 권한 확인
        if (!userStoreRoleService.canUserManageStore(userDetails.getId(), feedback.getStore().getId())) {
            throw new FeedbackException(ErrorMessage.STORE_ACCESS_DENIED);
        }

        // 조회 시 읽음 처리
        if (!feedback.isViewed()) {
            feedback.markAsViewed();
            feedbackRepository.save(feedback);
        }

        return mapToOwnerFeedbackResponse(feedback);
    }
}