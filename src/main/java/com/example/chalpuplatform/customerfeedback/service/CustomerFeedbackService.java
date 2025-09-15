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
    
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public FeedbackResponse createFeedback(Long userId, FeedbackCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        FoodItem foodItem = foodItemRepository.findById(request.getFoodId())
                .orElseThrow(() -> new FeedbackException(ErrorMessage.FOODITEM_NOT_FOUND));

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new FeedbackException(ErrorMessage.STORE_NOT_FOUND));

        Survey survey = surveyRepository.findById(request.getSurveyId())
                .orElseThrow(() -> new SurveyException(ErrorMessage.SURVEY_NOT_FOUND));

        CustomerFeedback feedback = CustomerFeedback.createFeedback(foodItem, store, user, survey);
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
                savedFeedback.getId(), userId, request.getFoodId());

        return mapToFeedbackResponse(savedFeedback);
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
    public List<FeedbackResponse> getUserFeedbacks(Long userId) {
        List<CustomerFeedback> feedbacks = feedbackRepository
                .findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);

        return feedbacks.stream()
                .map(this::mapToFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getUserFeedbacks(Long userId, Pageable pageable) {
        Page<CustomerFeedback> feedbacks = feedbackRepository
                .findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId, pageable);

        return feedbacks.map(this::mapToFeedbackResponse);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getStoreFeedbacks(Long storeId, Pageable pageable) {
        Page<CustomerFeedback> feedbacks = feedbackRepository
                .findByStoreIdAndIsActiveTrueOrderByCreatedAtDesc(storeId, pageable);

        return feedbacks.map(feedback -> mapToFeedbackResponseWithQuestionFilter(feedback, 9L));
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getFeedbackById(Long feedbackId) {
        CustomerFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new FeedbackException(ErrorMessage.FEEDBACK_NOT_FOUND));

        return mapToFeedbackResponse(feedback);
    }

    private FeedbackResponse mapToFeedbackResponse(CustomerFeedback feedback) {
        List<SurveyAnswer> answers = answerRepository.findByFeedbackIdOrderByQuestionId(feedback.getId());
        List<FeedbackPhoto> photos = photoRepository.findByFeedbackIdOrderByCreatedAtAsc(feedback.getId());

        FeedbackResponse response = FeedbackResponse.from(feedback);
        response.setSurveyAnswers(answers.stream()
                .map(SurveyAnswerResponse::from)
                .collect(Collectors.toList()));
        response.setPhotoUrls(photos.stream()
                .map(FeedbackPhoto::getImageUrl)
                .collect(Collectors.toList()));

        return response;
    }

    private FeedbackResponse mapToFeedbackResponseWithQuestionFilter(CustomerFeedback feedback, Long questionId) {
        // Repository에서 특정 questionId에 대한 답변만 조회
        Optional<SurveyAnswer> answerOpt = answerRepository.findByFeedbackIdAndQuestionId(feedback.getId(), questionId);
        List<FeedbackPhoto> photos = photoRepository.findByFeedbackIdOrderByCreatedAtAsc(feedback.getId());

        FeedbackResponse response = FeedbackResponse.from(feedback);

        // questionId에 해당하는 답변이 있으면 리스트에 추가
        if (answerOpt.isPresent()) {
            response.setSurveyAnswers(List.of(SurveyAnswerResponse.from(answerOpt.get())));
        } else {
            response.setSurveyAnswers(List.of());
        }

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

    public FeedbackPhotosPresignedUrlResponse generateMultipleFeedbackPhotosPresignedUrl(final Long userId, final FeedbackPhotosUploadRequest request) {
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
                                    userId, fileName, e.getMessage(), e);
                            throw new FeedbackException(ErrorMessage.PRESIGNED_URL_GENERATION_FAILED);
                        }
                    })
                    .collect(Collectors.toList());
            
            log.info("event=multiple_feedback_photos_presigned_urls_generated, user_id={}, file_count={}",
                    userId, request.getFileNames().size());
            
            return FeedbackPhotosPresignedUrlResponse.builder()
                    .photoUrls(photoUrls)
                    .build();
        } catch (Exception e) {
            log.error("event=multiple_feedback_photos_presigned_urls_generation_failed, user_id={}, file_count={}, error_message={}",
                    userId, request.getFileNames() != null ? request.getFileNames().size() : 0, e.getMessage(), e);
            throw new FeedbackException(ErrorMessage.PRESIGNED_URL_GENERATION_FAILED);
        }
    }

    public Page<FeedbackResponse> getFoodFeedbacks(Long foodId, Pageable pageable) {
        Page<CustomerFeedback> feedbacks = feedbackRepository
                .findByFoodItemIdAndIsActiveTrueOrderByCreatedAtDesc(foodId, pageable);

        return feedbacks.map(feedback -> mapToFeedbackResponseWithQuestionFilter(feedback, 9L));
    }
}