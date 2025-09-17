package com.example.chalpuplatform.photo.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.PhotoException;
import com.example.chalpuplatform.common.response.PageResponse;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.fooditem.repository.FoodItemRepository;
import com.example.chalpuplatform.photo.domain.Photo;
import com.example.chalpuplatform.photo.dto.*;
import com.example.chalpuplatform.photo.repository.PhotoRepository;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.repository.StoreRepository;
import com.example.chalpuplatform.store.service.UserStoreRoleService;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final UserStoreRoleService userStoreRoleService;
    private final FoodItemRepository foodItemRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudfrontDomain;

    public PhotoPresignedUrlResponse generatePresignedUrl(final Long userId, final PhotoUploadRequest request) {
        try {
            String s3Key = createS3Key(request.getFileName());
            URL presignedUrl = createPresignedUrl(s3Key);
            log.info("event=presigned_url_generated, user_id={}, file_name={}, s3_key={}",
                    userId, request.getFileName(), s3Key);
            return PhotoPresignedUrlResponse.builder()
                    .presignedUrl(presignedUrl.toString())
                    .s3Key(s3Key)
                    .build();
        } catch (Exception e) {
            log.error("event=presigned_url_generation_failed, user_id={}, file_name={}, error_message={}",
                    userId, request.getFileName(), e.getMessage(), e);
            throw new PhotoException(ErrorMessage.PRESIGNED_URL_GENERATION_FAILED);
        }
    }

    public PhotoPresignedUrlResponse generateTmpPresignedUrl(final Long userId, final PhotoUploadRequest request) {
        try {
            String s3Key = createTmpS3Key(request.getFileName());
            URL presignedUrl = createPresignedUrl(s3Key);
            log.info("event=tmp_presigned_url_generated, user_id={}, file_name={}, s3_key={}",
                    userId, request.getFileName(), s3Key);
            return PhotoPresignedUrlResponse.builder()
                    .presignedUrl(presignedUrl.toString())
                    .s3Key(s3Key)
                    .build();
        } catch (Exception e) {
            log.error("event=tmp_presigned_url_generation_failed, user_id={}, file_name={}, error_message={}",
                    userId, request.getFileName(), e.getMessage(), e);
            throw new PhotoException(ErrorMessage.PRESIGNED_URL_GENERATION_FAILED);
        }
    }

    @Transactional
    public PhotoResponse registerPhoto(final Long userId, final PhotoRegisterRequest request) {
        try {
            FoodItem foodItem = null;
            if (request.getFoodItemId() != null) {
                foodItem = foodItemRepository.findById(request.getFoodItemId())
                        .orElseThrow(() -> new PhotoException(ErrorMessage.FOODITEM_NOT_FOUND));
            }
            Photo photo = Photo.builder()
                    .s3Key(request.getS3Key())
                    .fileName(request.getFileName())
                    .foodItem(foodItem)
                    .fileSize(request.getFileSize())
                    .imageWidth(request.getImageWidth())
                    .imageHeight(request.getImageHeight())
                    .isActive(true)
                    .build();
            Photo savedPhoto = photoRepository.save(photo);
            
            // FoodItem의 thumbnailUrl 자동 업데이트 (일대일 관계)
            if (foodItem != null) {
                String photoUrl = cloudfrontDomain + "/" + savedPhoto.getS3Key();
                foodItem.setThumbnailUrl(photoUrl);
                foodItemRepository.save(foodItem);
                log.info("event=food_item_thumbnail_updated, food_item_id={}, thumbnail_url={}", 
                        foodItem.getId(), photoUrl);
            }
            
            log.info("event=photo_registered, photo_id={}, s3_key={}, user_id={}",
                    savedPhoto.getId(), savedPhoto.getS3Key(), userId);
            return PhotoResponse.from(savedPhoto, cloudfrontDomain);
        } catch (Exception e) {
            log.error("event=photo_registration_failed, s3_key={}, user_id={}, error_message={}",
                    request.getS3Key(), userId, e.getMessage(), e);
            throw new PhotoException(ErrorMessage.PHOTO_REGISTRATION_FAILED);
        }
    }

    public PageResponse<PhotoResponse> getPhotosByStore(final Long storeId, final Pageable pageable) {
        try {
            // Fetch join으로 N+1 문제 방지하면서 페이징 처리
            Page<Photo> photoPage = photoRepository.findByStoreIdWithFetchJoin(storeId, pageable);
            return PageResponse.from(photoPage.map(photo -> PhotoResponse.from(photo, cloudfrontDomain)));
        } catch (Exception e) {
            log.error("event=photos_by_store_failed, store_id={}, error_message={}",
                    storeId, e.getMessage(), e);
            throw new PhotoException(ErrorMessage.PHOTO_NOT_FOUND);
        }
    }

    public PageResponse<PhotoResponse> getPhotosByFoodItem(final Long foodItemId, final Pageable pageable) {
        try {
            Page<Photo> photoPage = photoRepository.findByFoodItemIdAndIsActiveTrueWithoutJoin(foodItemId, pageable);
            return PageResponse.from(photoPage.map(photo -> PhotoResponse.from(photo, cloudfrontDomain)));
        } catch (Exception e) {
            log.error("event=photos_by_food_item_failed, food_item_id={}, error_message={}",
                    foodItemId, e.getMessage(), e);
            throw new PhotoException(ErrorMessage.PHOTO_NOT_FOUND);
        }
    }

    public PhotoResponse getPhoto(final Long photoId) {
        try {
            Photo photo = findPhotoByIdWithoutJoin(photoId);
            return PhotoResponse.from(photo, cloudfrontDomain);
        } catch (Exception e) {
            log.error("event=photo_get_failed, photo_id={}, error_message={}",
                    photoId, e.getMessage(), e);
            throw new PhotoException(ErrorMessage.PHOTO_NOT_FOUND);
        }
    }

    @Transactional
    public void deletePhoto(final Long userId, final Long photoId) {
        try {
            // fetch join으로 FoodItem과 Store를 한번에 조회
            Photo photo = photoRepository.findByIdWithFoodItemAndStore(photoId)
                    .orElseThrow(() -> new PhotoException(ErrorMessage.PHOTO_NOT_FOUND));
            
            // FoodItem을 통해 Store 접근 (이미 fetch되어 있음)
            if (photo.getFoodItem() != null && photo.getFoodItem().getStore() != null) {
                Long storeId = photo.getFoodItem().getStore().getId();
                if (!userStoreRoleService.canUserManageStore(userId, storeId)) {
                    throw new PhotoException(ErrorMessage.STORE_ACCESS_DENIED);
                }
            }
            deleteS3Object(photo.getS3Key());
            photo.softDelete();
            log.info("event=photo_deleted, photo_id={}, user_id={}", photoId, userId);
        } catch (PhotoException e) {
            throw e;
        } catch (Exception e) {
            log.error("event=photo_deletion_failed, photo_id={}, user_id={}, error_message={}",
                    photoId, userId, e.getMessage(), e);
            throw new PhotoException(ErrorMessage.PHOTO_DELETE_FAILED);
        }
    }

    private Photo findPhotoByIdWithoutJoin(final Long photoId) {
        return photoRepository.findByIdAndIsActiveTrueWithoutJoin(photoId)
                .orElseThrow(() -> new PhotoException(ErrorMessage.PHOTO_NOT_FOUND));
    }

    private void deleteS3Object(final String s3Key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("event=s3_object_deleted, s3_key={}", s3Key);
        } catch (Exception e) {
            log.error("event=s3_object_deletion_failed, s3_key={}, error_message={}",
                    s3Key, e.getMessage(), e);
        }
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

    private String createS3Key(final String fileName) {
        Objects.requireNonNull(fileName, "fileName must not be null");
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new PhotoException(ErrorMessage.PHOTO_INVALID_FORMAT);
        }
        final String fileExtension = fileName.substring(lastDotIndex);
        return "platform/" + UUID.randomUUID() + fileExtension;
    }

    private String createTmpS3Key(final String fileName) {
        Objects.requireNonNull(fileName, "fileName must not be null");
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new PhotoException(ErrorMessage.PHOTO_INVALID_FORMAT);
        }
        final String fileExtension = fileName.substring(lastDotIndex);
        return "platform/" + UUID.randomUUID() + fileExtension;
    }

}