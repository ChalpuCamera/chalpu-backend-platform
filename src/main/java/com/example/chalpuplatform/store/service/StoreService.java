package com.example.chalpuplatform.store.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.StoreException;
import com.example.chalpuplatform.common.response.PageResponse;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.photo.repository.PhotoRepository;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.domain.UserStoreRole;
import com.example.chalpuplatform.store.dto.StoreRequest;
import com.example.chalpuplatform.store.dto.StoreResponse;
import com.example.chalpuplatform.store.repository.StoreRepository;
import com.example.chalpuplatform.store.repository.UserStoreRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserStoreRoleRepository userStoreRoleRepository;
    private final PhotoRepository photoRepository;

    @Transactional(readOnly = true)
    public StoreResponse getStore(Long storeId) {
        try {
            Store store = storeRepository.findByIdAndIsActiveTrue(storeId)
                    .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));
            return StoreResponse.from(store);
        } catch (Exception e) {
            log.error("event=store_get_failed, store_id={}, error_message={}", storeId, e.getMessage(), e);
            throw e;
        }
    }

    public StoreResponse createStore(StoreRequest storeRequest) {
        try {
            Store store = Store.createStore(storeRequest);
            Store savedStore = storeRepository.save(store);
            log.info("event=store_created, store_id={}", savedStore.getId());
            return StoreResponse.from(savedStore);
        } catch (Exception e) {
            log.error("event=store_creation_failed, store_name={}, error_message={}",
                    storeRequest.getStoreName(), e.getMessage(), e);
            throw new StoreException(ErrorMessage.STORE_CREATE_FAILED);
        }
    }

    public StoreResponse updateStore(Long storeId, StoreRequest storeRequest) {
        try {
            Store store = storeRepository.findByIdAndIsActiveTrue(storeId)
                    .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));
            
            store.updateStore(storeRequest);
            log.info("event=store_updated, store_id={}", storeId);
            return StoreResponse.from(store);
        } catch (Exception e) {
            log.error("event=store_update_failed, store_id={}, error_message={}",
                    storeId, e.getMessage(), e);
            throw new StoreException(ErrorMessage.STORE_UPDATE_FAILED);
        }
    }

    public void deleteStore(Long storeId) {
        try {
            Store store = storeRepository.findByIdAndIsActiveTrue(storeId)
                    .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));
            
            // UserStoreRole 소프트 삭제
            List<UserStoreRole> userStoreRoles = userStoreRoleRepository.findByStoreId(storeId);
            userStoreRoles.forEach(UserStoreRole::softDelete);

            // Store의 FoodItem들 소프트 삭제 및 연관된 Photo 삭제
            List<Long> foodItemIds = store.getFoodItems().stream()
                    .map(FoodItem::getId)
                    .toList();
            
            // Photo 벌크 삭제
            if (!foodItemIds.isEmpty()) {
                photoRepository.softDeleteByFoodItemIds(foodItemIds);
            }
            
            // FoodItem 소프트 삭제
            store.getFoodItems().forEach(FoodItem::softDelete);
            
            log.info("event=all_store_related_entities_soft_deleted, store_id={}, user_roles={}, food_items={}",
                    storeId, userStoreRoles.size(), store.getFoodItems().size());

            store.softDelete();
            log.info("event=store_soft_deleted, store_id={}", storeId);
        } catch (Exception e) {
            log.error("event=store_deletion_failed, store_id={}, error_message={}",
                    storeId, e.getMessage(), e);
            throw new StoreException(ErrorMessage.STORE_DELETE_FAILED);
        }
    }

    public PageResponse<StoreResponse> getAllStores(Pageable pageable) {
        return PageResponse.from(
                storeRepository.findByIsActiveTrue(pageable).map(StoreResponse::from)
        );
    }

    public StoreResponse updateStoreThumbnail(Long storeId, String photoUrl, Long userId) {
        try {
            Store store = storeRepository.findByIdAndIsActiveTrue(storeId)
                    .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));

            store.setThumbnailUrl(photoUrl);
            Store savedStore = storeRepository.save(store);

            log.info("매장 대표 사진 설정: storeId={}, photoUrl={}, userId={}", storeId, photoUrl, userId);
            return StoreResponse.from(savedStore);
        } catch (Exception e) {
            log.error("event=store_thumbnail_update_failed, store_id={}, error_message={}",
                    storeId, e.getMessage(), e);
            throw new StoreException(ErrorMessage.STORE_UPDATE_FAILED);
        }
    }
}