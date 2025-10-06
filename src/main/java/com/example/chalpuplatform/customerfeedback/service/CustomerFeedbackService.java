package com.example.chalpuplatform.customerfeedback.service;

import com.example.chalpuplatform.customerfeedback.domain.CustomerFeedback;
import com.example.chalpuplatform.customerfeedback.domain.FeedbackPhoto;
import com.example.chalpuplatform.customerfeedback.dto.*;
import com.example.chalpuplatform.customerfeedback.repository.CustomerFeedbackRepository;
import com.example.chalpuplatform.customerfeedback.repository.FeedbackPhotoRepository;

import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.user.domain.UserProfile;
import com.example.chalpuplatform.user.repository.UserRepository;
import com.example.chalpuplatform.user.repository.UserProfileRepository;

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
import com.example.chalpuplatform.common.exception.CampaignException;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.fooditem.repository.FoodItemRepository;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.repository.StoreRepository;
import com.example.chalpuplatform.store.service.UserStoreRoleService;
import com.example.chalpuplatform.user.domain.CustomerTaste;
import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import com.example.chalpuplatform.campaign.domain.Campaign;
import com.example.chalpuplatform.campaign.repository.CampaignRepository;
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
import java.util.Collections;
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
    private final UserProfileRepository userProfileRepository;
    private final FoodItemRepository foodItemRepository;
    private final StoreRepository storeRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository questionRepository;
    private final S3Presigner s3Presigner;
    private final UserStoreRoleService userStoreRoleService;
    private final CampaignRepository campaignRepository;
    
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

        // 캠페인 처리: 직접 ID로 받거나 활성 캠페인 자동 검색
        Campaign campaign = null;
        if (request.getCampaignId() != null) {
            // 캠페인 ID가 명시적으로 제공된 경우
            campaign = campaignRepository.findById(request.getCampaignId())
                    .orElseThrow(() -> new CampaignException(ErrorMessage.CAMPAIGN_NOT_FOUND));

            // 캠페인이 해당 매장/음식과 일치하는지 검증
            if (!campaign.getStore().getId().equals(store.getId()) || !campaign.getFoodItem().getId().equals(foodItem.getId())) {
                throw new CampaignException(ErrorMessage.CAMPAIGN_MISMATCH);
            }
        }
        // 캠페인 ID가 없으면 그냥 null로 처리

        CustomerFeedback feedback = CustomerFeedback.builder()
                .foodItem(foodItem)
                .store(store)
                .user(user)
                .survey(survey)
                .campaign(campaign)  // 캠페인 연결
                .isViewed(false)  // 새 피드백은 읽지 않은 상태로 설정
                .build();

        // 현재 고객 입맛을 스냅샷으로 저장
        userProfileRepository.findByUserId(user.getId()).ifPresent(userProfile -> {
            if (userProfile.getCustomerTaste() != null) {
                CustomerTaste taste = userProfile.getCustomerTaste();
                feedback.setSpicyLevelSnapshot(taste.getSpicyLevel());
                feedback.setMealAmountSnapshot(taste.getMealAmount());
                feedback.setMealSpendingSnapshot(taste.getMealSpending());
            }
        });

        CustomerFeedback savedFeedback = feedbackRepository.save(feedback);

        // Store와 FoodItem의 피드백 카운트 원자적 증가
        storeRepository.incrementFeedbackCount(store.getId());
        foodItemRepository.incrementFeedbackCount(foodItem.getId());
        log.info("피드백 카운트 증가: storeId={}, foodItemId={}, feedbackId={}",
                store.getId(), foodItem.getId(), savedFeedback.getId());

        // 캠페인 연결 시 카운트 원자적 증가
        if (campaign != null) {
            campaignRepository.incrementFeedbackCount(campaign.getId());
            log.info("캠페인 피드백 카운트 증가: campaignId={}, feedbackId={}",
                    campaign.getId(), savedFeedback.getId());
        }

        saveSurveyAnswers(savedFeedback, request.getSurveyAnswers());

        if (request.getPhotoS3Keys() != null && !request.getPhotoS3Keys().isEmpty()) {
            saveFeedbackPhotos(savedFeedback, request.getPhotoS3Keys());
        }

        userProfileRepository.findByUserId(user.getId()).ifPresent(userProfile -> {
            userProfile.incrementFeedbackCount();
            userProfile.incrementRewardCount();
            userProfileRepository.save(userProfile);
        });

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
    public Page<CustomerFeedbackResponse> getUserFeedbacks(UserDetailsImpl userDetails, Pageable pageable) {
        // 페치 조인으로 연관 엔티티 함께 조회
        Page<CustomerFeedback> feedbacks = feedbackRepository
                .findByUserIdWithDetails(userDetails.getId(), pageable);

        if (feedbacks.isEmpty()) {
            return Page.empty();
        }

        // 피드백 ID 추출
        List<Long> feedbackIds = feedbacks.stream()
                .map(CustomerFeedback::getId)
                .collect(Collectors.toList());

        // 답변 배치 조회 (question 포함)
        Map<Long, List<SurveyAnswer>> answersMap = answerRepository
                .findAnswersByFeedbackIdsWithQuestion(feedbackIds)
                .stream()
                .collect(Collectors.groupingBy(sa -> sa.getFeedback().getId()));

        // 사진 배치 조회
        Map<Long, List<FeedbackPhoto>> photosMap = photoRepository
                .findPhotosByFeedbackIds(feedbackIds)
                .stream()
                .collect(Collectors.groupingBy(fp -> fp.getFeedback().getId()));

        // DTO 변환
        return feedbacks.map(feedback -> mapToCustomerFeedbackResponseWithData(
                feedback,
                answersMap.getOrDefault(feedback.getId(), Collections.emptyList()),
                photosMap.getOrDefault(feedback.getId(), Collections.emptyList())
        ));
    }

    @Transactional(readOnly = true)
    public Page<OwnerFeedbackResponse> getStoreFeedbacks(Long storeId, UserDetailsImpl userDetails, Pageable pageable) {
        // 사장님 권한 확인
        if (!userStoreRoleService.canUserManageStore(userDetails.getId(), storeId)) {
            throw new FeedbackException(ErrorMessage.STORE_ACCESS_DENIED);
        }

        Page<CustomerFeedback> feedbacks = feedbackRepository
                .findByStoreIdWithDetails(storeId, pageable);
        if (feedbacks.isEmpty()) {
            return Page.empty();
        }

        // 피드백 ID 추출
        List<Long> feedbackIds = feedbacks.stream()
                .map(CustomerFeedback::getId)
                .collect(Collectors.toList());

        // 사장님 메시지만 효율적으로 조회
        Map<Long, String> ownerMessagesMap = answerRepository
                .findOwnerMessagesByFeedbackIds(feedbackIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],    // feedback_id
                        row -> (String) row[1]    // answer_text
                ));

        // 사진 배치 조회
        Map<Long, List<String>> photosMap = photoRepository
                .findPhotosByFeedbackIds(feedbackIds)
                .stream()
                .collect(Collectors.groupingBy(
                        fp -> fp.getFeedback().getId(),
                        Collectors.mapping(FeedbackPhoto::getImageUrl, Collectors.toList())
                ));

        // DTO 변환
        return feedbacks.map(feedback -> {
            OwnerFeedbackResponse response = OwnerFeedbackResponse.from(feedback);
            response.setOwnerMessage(ownerMessagesMap.get(feedback.getId()));
            response.setPhotoUrls(photosMap.getOrDefault(feedback.getId(), Collections.emptyList()));
            return response;
        });
    }

    @Transactional(readOnly = true)
    public CustomerFeedbackResponse getFeedbackById(Long feedbackId) {
        CustomerFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new FeedbackException(ErrorMessage.FEEDBACK_NOT_FOUND));

        // 답변은 question과 함께 조회
        List<SurveyAnswer> answers = answerRepository.findByFeedbackIdWithQuestion(feedbackId);

        // 사진 조회
        List<FeedbackPhoto> photos = photoRepository.findByFeedbackIdOrderByCreatedAtAsc(feedbackId);

        return mapToCustomerFeedbackResponseWithData(feedback, answers, photos);
    }


    // 고객용 응답 매핑 (고객 정보 제외) - 데이터를 받아서 처리
    private CustomerFeedbackResponse mapToCustomerFeedbackResponseWithData(CustomerFeedback feedback,
                                                                          List<SurveyAnswer> answers,
                                                                          List<FeedbackPhoto> photos) {
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

    // 기존 메서드는 단일 조회용으로 유지
    private CustomerFeedbackResponse mapToCustomerFeedbackResponse(CustomerFeedback feedback) {
        List<SurveyAnswer> answers = answerRepository.findByFeedbackIdOrderByQuestionId(feedback.getId());
        List<FeedbackPhoto> photos = photoRepository.findByFeedbackIdOrderByCreatedAtAsc(feedback.getId());
        return mapToCustomerFeedbackResponseWithData(feedback, answers, photos);
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

    @Transactional(readOnly = true)
    public Page<OwnerFeedbackResponse> getFoodFeedbacks(Long foodId, UserDetailsImpl userDetails, Pageable pageable) {
        // 음식이 속한 매장 조회
        FoodItem foodItem = foodItemRepository.findById(foodId)
                .orElseThrow(() -> new FeedbackException(ErrorMessage.FOODITEM_NOT_FOUND));

        // 사장님 권한 확인
        if (!userStoreRoleService.canUserManageStore(userDetails.getId(), foodItem.getStore().getId())) {
            throw new FeedbackException(ErrorMessage.STORE_ACCESS_DENIED);
        }

        // 페치 조인으로 연관 엔티티 함께 조회
        Page<CustomerFeedback> feedbacks = feedbackRepository
                .findByFoodItemIdWithDetails(foodId, pageable);

        if (feedbacks.isEmpty()) {
            return Page.empty();
        }

        // 피드백 ID 추출
        List<Long> feedbackIds = feedbacks.stream()
                .map(CustomerFeedback::getId)
                .collect(Collectors.toList());

        // 사장님 메시지만 효율적으로 조회
        Map<Long, String> ownerMessagesMap = answerRepository
                .findOwnerMessagesByFeedbackIds(feedbackIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],    // feedback_id
                        row -> (String) row[1]    // answer_text
                ));

        // 사진 배치 조회
        Map<Long, List<String>> photosMap = photoRepository
                .findPhotosByFeedbackIds(feedbackIds)
                .stream()
                .collect(Collectors.groupingBy(
                        fp -> fp.getFeedback().getId(),
                        Collectors.mapping(FeedbackPhoto::getImageUrl, Collectors.toList())
                ));

        // DTO 변환
        return feedbacks.map(feedback -> {
            OwnerFeedbackResponse response = OwnerFeedbackResponse.from(feedback);
            response.setOwnerMessage(ownerMessagesMap.get(feedback.getId()));
            response.setPhotoUrls(photosMap.getOrDefault(feedback.getId(), Collections.emptyList()));
            return response;
        });
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