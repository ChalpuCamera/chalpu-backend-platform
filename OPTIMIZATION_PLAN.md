# 최적화 및 리팩토링 계획

## 📋 프로젝트 분석 결과

프로젝트 코드베이스를 분석한 결과, 성능 최적화와 아키텍처 패턴 개선이 필요한 여러 영역을 발견했습니다.

## 1. JPA 성능 최적화

### 1.0 Campaign 엔티티 최적화 추가

**현재 상황**
- Campaign 엔티티가 Store, FoodItem과 Lazy Loading 관계
- CampaignQueryService에서 여러 번 DB 호출 발생 가능
- N+1 문제 발생 위험

**개선 방안**

#### Campaign 엔티티에 @NamedEntityGraph 추가
```java
@NamedEntityGraph(
    name = "Campaign.withStoreAndFoodItem",
    attributeNodes = {
        @NamedAttributeNode("store"),
        @NamedAttributeNode("foodItem")
    }
)
@NamedEntityGraph(
    name = "Campaign.detail",
    attributeNodes = {
        @NamedAttributeNode("store"),
        @NamedAttributeNode("foodItem")
    }
)
@Entity
public class Campaign extends BaseTimeEntity {
    // 기존 코드
}
```

#### CampaignRepository에 EntityGraph 적용
```java
@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    // EntityGraph를 사용한 단건 조회
    @EntityGraph("Campaign.withStoreAndFoodItem")
    Optional<Campaign> findWithDetailsById(Long id);

    // 페이징 조회시 EntityGraph 사용
    @EntityGraph("Campaign.withStoreAndFoodItem")
    Page<Campaign> findByStoreAndIsActiveTrue(Store store, Pageable pageable);

    // 상태별 조회시 EntityGraph 사용
    @EntityGraph("Campaign.withStoreAndFoodItem")
    Page<Campaign> findByStoreAndStatusAndIsActiveTrue(Store store, CampaignStatus status, Pageable pageable);
}
```

#### CustomerFeedback 엔티티 최적화
```java
@NamedEntityGraph(
    name = "CustomerFeedback.detail",
    attributeNodes = {
        @NamedAttributeNode("user"),
        @NamedAttributeNode("store"),
        @NamedAttributeNode("foodItem"),
        @NamedAttributeNode("survey")
    }
)
@Entity
public class CustomerFeedback extends BaseTimeEntity {
    // 기존 코드
}
```

### Entity Graph vs Fetch Join 선택 기준

> [!tip]
> **Entity Graph 사용이 적합한 경우**
> - 동일한 fetch 전략을 여러 메서드에서 재사용
> - 런타임에 동적으로 fetch 전략 변경 필요
> - JPA 표준 기능만 사용하고 싶을 때
> - 코드 재사용성과 유지보수성이 중요한 경우

> [!warning]
> **Fetch Join이 더 나은 경우**
> - 복잡한 WHERE 조건절과 함께 사용
> - 특정 쿼리에만 필요한 일회성 최적화
> - 집계 함수나 GROUP BY와 함께 사용
> - WHERE 절 필터링이 중요한 경우

#### 성능 최적화 추가 사항

**쿼리 최적화**
```java
// 배치 처리를 통한 최적화
@Service
public class CampaignQueryService {

    // 캠페인 목록 조회시 피드백 카운트를 배치로 조회
    public List<CampaignWithFeedbackCount> getCampaignsWithFeedbackCount(Long storeId) {
        List<Campaign> campaigns = campaignRepository.findByStoreIdAndIsActiveTrue(storeId);

        // 캠페인 ID 목록 추출
        List<Long> campaignIds = campaigns.stream()
            .map(Campaign::getId)
            .collect(Collectors.toList());

        // 한 번의 쿼리로 모든 피드백 카운트 조회
        Map<Long, Long> feedbackCounts = customerFeedbackRepository
            .countByFoodItemIdGroupByCampaignIds(campaignIds);

        // 결과 조합
        return campaigns.stream()
            .map(campaign -> new CampaignWithFeedbackCount(
                campaign,
                feedbackCounts.getOrDefault(campaign.getId(), 0L)
            ))
            .collect(Collectors.toList());
    }
}
```

### 1.1 Fetch Join을 @EntityGraph로 변경

**현재 상황**
- CustomerFeedbackRepository, SurveyAnswerRepository 등에서 JOIN FETCH 사용
- Hibernate가 Page + Fetch Join 조합에 대해 경고 메시지 출력 (실제 성능 문제는 없음)

**개선 방안**
```java
// 현재 코드 (CustomerFeedbackRepository)
@Query("""
    SELECT cf FROM CustomerFeedback cf
    LEFT JOIN FETCH cf.foodItem
    LEFT JOIN FETCH cf.store
    LEFT JOIN FETCH cf.user
    LEFT JOIN FETCH cf.survey
    WHERE cf.store.id = :storeId
    AND cf.isActive = true
""")
Page<CustomerFeedback> findByStoreIdWithDetails(@Param("storeId") Long storeId, Pageable pageable);

// 개선안: @EntityGraph 사용
@EntityGraph(attributePaths = {"foodItem", "store", "user", "survey"})
Page<CustomerFeedback> findByStoreIdAndIsActiveTrueOrderByCreatedAtDesc(Long storeId, Pageable pageable);
```

**변경 대상 Repository**
1. CustomerFeedbackRepository - 3개 메서드
2. SurveyAnswerRepository - 2개 메서드
3. PhotoRepository - 페치 조인 확인 필요
4. UserStoreRoleRepository - 페치 조인 확인 필요

### 1.2 @OneToMany 관계 Batch Size 최적화

**현재 상황**
- Store.foodItems: @OneToMany 관계로 N+1 문제 발생 가능
- Lazy Loading 시 각 Store마다 별도 쿼리 실행

**개선 방안**
```java
// Store 엔티티
@Entity
@BatchSize(size = 25) // 클래스 레벨 배치 설정
public class Store extends BaseTimeEntity {

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    @BatchSize(size = 50) // 컬렉션별 개별 설정
    private List<FoodItem> foodItems = new ArrayList<>();
}

// application.yml 전역 설정
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 20
        jdbc.batch_size: 25
```

**최적화 대상**
- Store.foodItems (현재 유일한 @OneToMany)
- 향후 추가될 @OneToMany 관계들

## 2. DDD (Domain-Driven Design) 패턴 적용

### 2.1 Rich Domain Model로 전환

**현재 문제점**
- Anemic Domain Model (빈약한 도메인 모델)
- 비즈니스 로직이 Service 계층에 분산되어 있음
- 엔티티가 단순 데이터 컨테이너 역할만 수행

**개선 방안**

#### Value Objects 도입
```java
// Price Value Object
public record Price(BigDecimal amount, String currency) {
    public Price {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("가격은 음수일 수 없습니다");
        }
    }

    public Price applyDiscount(BigDecimal discountRate) {
        return new Price(amount.multiply(BigDecimal.ONE.subtract(discountRate)), currency);
    }
}

// S3Key Value Object
public record S3Key(String bucket, String key) {
    public static S3Key generate(String bucket, String fileName) {
        String uuid = UUID.randomUUID().toString();
        return new S3Key(bucket, "photos/" + uuid + "/" + fileName);
    }

    public String getFullPath() {
        return bucket + "/" + key;
    }
}

// PhotoMetadata Value Object
public record PhotoMetadata(Integer width, Integer height, Long fileSize) {
    public boolean isValidDimensions() {
        return width > 0 && height > 0 && fileSize > 0;
    }
}
```

#### Domain Service 도입
```java
// 도메인 서비스 - 여러 엔티티에 걸친 비즈니스 규칙
@Service
public class FoodItemDomainService {
    public FoodItem createFoodItem(Store store, FoodItemCommand command) {
        validateStoreCapacity(store);
        validatePriceRange(command.getPrice());

        FoodItem foodItem = FoodItem.builder()
            .store(store)
            .name(command.getName())
            .price(new Price(command.getPrice(), "KRW"))
            .build();

        store.addFoodItem(foodItem);
        return foodItem;
    }

    private void validateStoreCapacity(Store store) {
        if (store.getFoodItems().size() >= 100) {
            throw new DomainException("가게당 최대 100개 메뉴만 등록 가능합니다");
        }
    }
}

// 도메인 서비스 - 피드백 생성 로직
@Service
public class CustomerFeedbackDomainService {
    public CustomerFeedback createFeedback(FeedbackCommand command) {
        CustomerFeedback feedback = new CustomerFeedback();

        // 도메인 규칙 적용
        feedback.validateEligibility(command.getUser());
        feedback.captureCustomerTasteSnapshot(command.getUserProfile());
        feedback.calculateRewardPoints();

        return feedback;
    }
}
```

#### Rich Domain Entity 구현
```java
@Entity
public class Store extends BaseTimeEntity {
    // 필드들...

    // 비즈니스 메서드
    public void addFoodItem(FoodItem foodItem) {
        validateFoodItemAddition(foodItem);
        this.foodItems.add(foodItem);
        foodItem.setStore(this);
    }

    public void removeFoodItem(FoodItem foodItem) {
        if (!foodItem.canBeDeleted()) {
            throw new DomainException("진행중인 주문이 있는 메뉴는 삭제할 수 없습니다");
        }
        this.foodItems.remove(foodItem);
    }

    public void updateDeliveryLinks(DeliveryPlatformLinks links) {
        validateDeliveryLinks(links);
        this.deliveryPlatformLinks = links;
    }

    private void validateFoodItemAddition(FoodItem item) {
        if (this.foodItems.size() >= 100) {
            throw new DomainException("메뉴는 최대 100개까지만 등록 가능합니다");
        }
    }
}

@Entity
public class FoodItem extends BaseTimeEntity {
    private Price price; // Value Object 사용

    public void updatePrice(Price newPrice) {
        if (hasActivePromotion() && newPrice.isHigherThan(this.price)) {
            throw new DomainException("프로모션 진행중에는 가격을 인상할 수 없습니다");
        }
        this.price = newPrice;
    }

    public boolean canBeDeleted() {
        return !hasActiveOrders() && !hasActivePromotion();
    }

    public void applyPromotion(Promotion promotion) {
        validatePromotion(promotion);
        this.currentPromotion = promotion;
        this.promotionPrice = this.price.applyDiscount(promotion.getDiscountRate());
    }
}
```

### 2.2 도메인 이벤트 도입

**개선 방안**
```java
// 도메인 이벤트
public record FeedbackCreatedEvent(
    Long feedbackId,
    Long userId,
    Long storeId,
    Integer rewardPoints
) implements DomainEvent {}

// 이벤트 발행
@Entity
public class CustomerFeedback extends BaseTimeEntity {
    @DomainEvents
    private List<DomainEvent> domainEvents = new ArrayList<>();

    public static CustomerFeedback create(FeedbackCommand command) {
        CustomerFeedback feedback = new CustomerFeedback();
        // ... 생성 로직

        // 이벤트 발행
        feedback.domainEvents.add(new FeedbackCreatedEvent(
            feedback.getId(),
            command.getUserId(),
            command.getStoreId(),
            1
        ));

        return feedback;
    }
}

// 이벤트 리스너
@Component
public class FeedbackEventHandler {
    @EventListener
    public void handleFeedbackCreated(FeedbackCreatedEvent event) {
        // 리워드 포인트 지급
        // 알림 발송
        // 통계 업데이트
    }
}
```

## 3. CQRS (Command Query Responsibility Segregation) 패턴 적용

### 3.1 서비스 레이어 명령/조회 분리

**현재 문제점**
- 하나의 서비스에 조회와 명령 로직이 혼재
- 복잡한 조회 요구사항과 비즈니스 로직이 결합되어 있음
- 성능 최적화가 어려움

**적용 대상 서비스**
1. CustomerFeedbackService - 가장 복잡한 서비스
2. FoodItemService - 조회와 명령이 명확히 구분
3. StoreService - 조회 빈도가 높음
4. RewardService - 포인트 조회와 사용 분리 필요

### 3.2 CustomerFeedback CQRS 구현

#### Query Service (조회 전용)
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerFeedbackQueryService {
    private final CustomerFeedbackRepository repository;

    // 사장님용 피드백 조회 (읽음 상태, 통계 포함)
    public PageResponse<OwnerFeedbackView> getOwnerFeedbacks(Long storeId, Pageable pageable) {
        // 최적화된 조회 쿼리
        // Projection 사용으로 필요한 필드만 조회
        return repository.findOwnerFeedbackViews(storeId, pageable);
    }

    // 고객용 피드백 히스토리
    public PageResponse<CustomerFeedbackHistory> getCustomerHistory(Long userId, Pageable pageable) {
        // 고객 관점의 간단한 조회
        return repository.findCustomerHistory(userId, pageable);
    }

    // 통계 조회 (캐싱 적용)
    @Cacheable(value = "feedbackStats", key = "#storeId")
    public FeedbackStatistics getStatistics(Long storeId, Period period) {
        return repository.calculateStatistics(storeId, period);
    }

    // 읽지 않은 피드백 개수
    public UnreadCountResponse getUnreadCounts(Long storeId) {
        return repository.getUnreadCountsByStore(storeId);
    }
}
```

#### Command Service (명령 처리)
```java
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerFeedbackCommandService {
    private final CustomerFeedbackDomainService domainService;
    private final CustomerFeedbackRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public CustomerFeedbackResponse createFeedback(CreateFeedbackCommand command) {
        // 도메인 서비스를 통한 생성
        CustomerFeedback feedback = domainService.createFeedback(command);

        // 저장
        CustomerFeedback saved = repository.save(feedback);

        // 이벤트 발행
        eventPublisher.publishEvent(new FeedbackCreatedEvent(saved));

        return CustomerFeedbackResponse.from(saved);
    }

    public void markAsViewed(Long feedbackId, Long userId) {
        CustomerFeedback feedback = repository.findById(feedbackId)
            .orElseThrow(() -> new NotFoundException("피드백을 찾을 수 없습니다"));

        feedback.markAsViewed(userId);
        repository.save(feedback);
    }

    public void updateFeedbackStatus(Long feedbackId, FeedbackStatus status) {
        CustomerFeedback feedback = repository.findById(feedbackId)
            .orElseThrow(() -> new NotFoundException("피드백을 찾을 수 없습니다"));

        feedback.updateStatus(status);
        repository.save(feedback);

        eventPublisher.publishEvent(new FeedbackStatusChangedEvent(feedbackId, status));
    }
}
```

#### Read Model (조회 전용 모델)
```java
// 사장님용 피드백 조회 모델
public class OwnerFeedbackView {
    private Long feedbackId;
    private String customerName;
    private String foodItemName;
    private Integer rating;
    private String comment;
    private Boolean isViewed;
    private LocalDateTime createdAt;
    private List<SurveyAnswerSummary> surveyAnswers;
    // Projection으로 DB에서 직접 조회
}

// 고객용 피드백 히스토리
public class CustomerFeedbackHistory {
    private Long feedbackId;
    private String storeName;
    private String foodItemName;
    private Integer earnedPoints;
    private LocalDateTime createdAt;
    // 고객에게 필요한 정보만 포함
}

// 통계 모델
public class FeedbackStatistics {
    private Integer totalCount;
    private Double averageRating;
    private Map<Integer, Integer> ratingDistribution;
    private List<TopFeedbackItem> topItems;
    private TrendData trend;
}
```

### 3.3 FoodItem CQRS 구현

#### Query Service
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FoodItemQueryService {
    private final FoodItemRepository repository;

    // 고객용 메뉴 조회 (활성 메뉴만, 가격 정보 포함)
    public PageResponse<FoodItemMenuView> getMenuForCustomers(Long storeId, Pageable pageable) {
        return repository.findActiveMenuItems(storeId, pageable);
    }

    // 사장님용 메뉴 관리 조회 (모든 상태, 통계 포함)
    public PageResponse<FoodItemManagementView> getMenuForManagement(Long storeId, Pageable pageable) {
        return repository.findManagementView(storeId, pageable);
    }

    // 메뉴 검색
    @Cacheable(value = "foodSearch", key = "#criteria.hashCode()")
    public List<FoodItemSearchResult> searchFoodItems(SearchCriteria criteria) {
        return repository.searchWithCriteria(criteria);
    }

    // 베스트 메뉴
    public List<BestFoodItem> getBestItems(Long storeId, int limit) {
        return repository.findBestItems(storeId, limit);
    }
}
```

#### Command Service
```java
@Service
@RequiredArgsConstructor
@Transactional
public class FoodItemCommandService {
    private final FoodItemDomainService domainService;
    private final FoodItemRepository repository;
    private final StoreRepository storeRepository;

    public FoodItemResponse createFoodItem(CreateFoodItemCommand command) {
        Store store = storeRepository.findById(command.getStoreId())
            .orElseThrow(() -> new NotFoundException("가게를 찾을 수 없습니다"));

        // 도메인 서비스를 통한 생성
        FoodItem foodItem = domainService.createFoodItem(store, command);

        FoodItem saved = repository.save(foodItem);
        return FoodItemResponse.from(saved);
    }

    public void updateFoodItem(UpdateFoodItemCommand command) {
        FoodItem foodItem = repository.findById(command.getFoodItemId())
            .orElseThrow(() -> new NotFoundException("메뉴를 찾을 수 없습니다"));

        // 도메인 로직 수행
        foodItem.update(command);
        repository.save(foodItem);
    }

    public void deleteFoodItem(Long foodItemId, Long userId) {
        FoodItem foodItem = repository.findById(foodItemId)
            .orElseThrow(() -> new NotFoundException("메뉴를 찾을 수 없습니다"));

        // 도메인 규칙 검증
        if (!foodItem.canBeDeleted()) {
            throw new BusinessException("삭제할 수 없는 메뉴입니다");
        }

        foodItem.softDelete();
        repository.save(foodItem);
    }
}
```

### 3.4 Repository 레벨 CQRS

#### Query Repository
```java
@Repository
public interface CustomerFeedbackQueryRepository {
    // Projection 기반 조회
    @Query("""
        SELECT new com.example.dto.OwnerFeedbackView(
            cf.id, u.name, fi.foodName, cf.rating, cf.comment, cf.isViewed, cf.createdAt
        )
        FROM CustomerFeedback cf
        JOIN cf.user u
        JOIN cf.foodItem fi
        WHERE cf.store.id = :storeId
        ORDER BY cf.createdAt DESC
    """)
    Page<OwnerFeedbackView> findOwnerFeedbackViews(@Param("storeId") Long storeId, Pageable pageable);

    // Native Query for 복잡한 통계
    @Query(nativeQuery = true, value = """
        SELECT
            COUNT(*) as total_count,
            AVG(rating) as avg_rating,
            COUNT(CASE WHEN is_viewed = false THEN 1 END) as unread_count
        FROM customer_feedback
        WHERE store_id = :storeId
        AND created_at >= :startDate
    """)
    FeedbackStatistics calculateStatistics(@Param("storeId") Long storeId, @Param("startDate") LocalDateTime startDate);
}
```

#### Command Repository
```java
@Repository
public interface CustomerFeedbackCommandRepository extends JpaRepository<CustomerFeedback, Long> {
    // 단순 CRUD 작업만 수행
    // 복잡한 조회는 QueryRepository로 분리
}
```

## 4. 기타 코드 개선사항

### 4.1 엔티티 @Data 어노테이션 제거

**현재 문제점**
- JPA 엔티티에 @Data 사용은 안티패턴 (equals/hashCode 문제, 양방향 연관관계 순환참조)

**개선 방안**
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Store extends BaseTimeEntity {
    // @Setter는 필요한 필드에만 선택적으로 적용
}
```

### 4.2 트랜잭션 범위 최적화

**개선 방안**
```java
@Service
@RequiredArgsConstructor
public class StoreService {
    // 클래스 레벨 @Transactional 제거

    @Transactional(readOnly = true)
    public StoreResponse getStore(Long storeId) {
        // 조회 로직
    }

    @Transactional
    public StoreResponse createStore(StoreRequest request) {
        // 생성 로직
    }
}
```

### 4.3 벌크 연산 최적화

**개선 방안**
```java
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    @Modifying
    @Query("UPDATE Photo p SET p.isActive = false WHERE p.foodItem.id IN :foodItemIds")
    void softDeleteByFoodItemIds(@Param("foodItemIds") List<Long> foodItemIds);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Photo p WHERE p.createdAt < :date AND p.isTemporary = true")
    void deleteOldTemporaryPhotos(@Param("date") LocalDateTime date);
}
```

## 5. 구현 우선순위

### Phase 1: 성능 최적화 (1-2주)
1. N+1 문제 해결
2. 페치 조인 최적화
3. 벌크 연산 구현

### Phase 2: DDD 패턴 적용 (2-3주)
1. Value Objects 도입
2. Rich Domain Model 전환
3. Domain Service 계층 추가

### Phase 3: CQRS 패턴 적용 (2-3주)
1. 핵심 서비스부터 단계적 분리
2. Read Model 구현
3. 이벤트 소싱 고려 (선택사항)

### Phase 4: 코드 품질 개선 (1주)
1. 엔티티 어노테이션 정리
2. 트랜잭션 최적화
3. 테스트 코드 보강

## 6. 예상 효과

- **성능 향상**: 쿼리 최적화로 30-50% 응답시간 단축 예상
- **유지보수성**: 명확한 책임 분리로 코드 이해도 향상
- **확장성**: DDD와 CQRS 패턴으로 비즈니스 로직 확장 용이
- **테스트 용이성**: 도메인 로직 분리로 단위 테스트 작성 간소화

## 7. 주의사항

- 단계적 적용으로 리스크 최소화
- 각 단계별 성능 측정 및 모니터링
- 기존 API 호환성 유지
- 충분한 테스트 코드 작성 후 리팩토링 진행