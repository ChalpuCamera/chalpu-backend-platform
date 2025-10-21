package com.example.chalpuplatform.subscription.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.StoreException;
import com.example.chalpuplatform.common.exception.SubscriptionException;
import com.example.chalpuplatform.store.repository.StoreRepository;
import com.example.chalpuplatform.subscription.domain.StoreSubscription;
import com.example.chalpuplatform.subscription.dto.SubscribeRequest;
import com.example.chalpuplatform.subscription.dto.SubscriptionResponse;
import com.example.chalpuplatform.subscription.repository.StoreSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreSubscriptionService {

    private final StoreSubscriptionRepository subscriptionRepository;
    private final StoreRepository storeRepository;

    public SubscriptionResponse subscribe(Long userId, SubscribeRequest request) {
        storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));

        StoreSubscription existingSubscription = subscriptionRepository
                .findByUserIdAndStoreId(userId, request.getStoreId())
                .orElse(null);

        if (existingSubscription != null) {
            if (existingSubscription.getIsActive()) {
                throw new SubscriptionException(ErrorMessage.SUBSCRIPTION_ALREADY_ACTIVE);
            }
            existingSubscription.reactivate();
            log.info("구독 재활성화: userId={}, storeId={}", userId, request.getStoreId());
            return SubscriptionResponse.from(existingSubscription, "구독이 재활성화되었습니다.");
        }

        StoreSubscription newSubscription = StoreSubscription.create(userId, request.getStoreId());
        subscriptionRepository.save(newSubscription);

        log.info("구독 생성: userId={}, storeId={}", userId, request.getStoreId());

        return SubscriptionResponse.from(newSubscription, "구독이 완료되었습니다.");
    }

    public SubscriptionResponse unsubscribe(Long userId, Long storeId) {
        StoreSubscription subscription = subscriptionRepository
                .findByUserIdAndStoreId(userId, storeId)
                .orElseThrow(() -> new SubscriptionException(ErrorMessage.SUBSCRIPTION_NOT_FOUND));

        subscription.unsubscribe();

        log.info("구독 취소: userId={}, storeId={}", userId, storeId);

        return SubscriptionResponse.from(subscription, "구독이 취소되었습니다.");
    }

    public SubscriptionResponse toggleNotification(Long userId, Long storeId) {
        StoreSubscription subscription = subscriptionRepository
                .findByUserIdAndStoreId(userId, storeId)
                .orElseThrow(() -> new SubscriptionException(ErrorMessage.SUBSCRIPTION_NOT_FOUND));

        if (subscription.getNotificationEnabled()) {
            subscription.disableNotification();
            log.info("알림 비활성화: userId={}, storeId={}", userId, storeId);
            return SubscriptionResponse.from(subscription, "알림이 비활성화되었습니다.");
        } else {
            subscription.enableNotification();
            log.info("알림 활성화: userId={}, storeId={}", userId, storeId);
            return SubscriptionResponse.from(subscription, "알림이 활성화되었습니다.");
        }
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(Long userId, Long storeId) {
        StoreSubscription subscription = subscriptionRepository
                .findByUserIdAndStoreId(userId, storeId)
                .orElseThrow(() -> new SubscriptionException(ErrorMessage.SUBSCRIPTION_NOT_FOUND));

        return SubscriptionResponse.from(subscription, "구독 정보 조회 성공");
    }

    @Transactional(readOnly = true)
    public boolean isSubscribed(Long userId, Long storeId) {
        return subscriptionRepository.existsByUserIdAndStoreIdAndIsActiveTrue(userId, storeId);
    }
}
