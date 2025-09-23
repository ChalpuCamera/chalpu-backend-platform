package com.example.chalpuplatform.campaign.service;

import com.example.chalpuplatform.campaign.domain.Campaign;
import com.example.chalpuplatform.campaign.dto.CampaignDetailResponse;
import com.example.chalpuplatform.campaign.dto.CampaignResponse;
import com.example.chalpuplatform.campaign.dto.CampaignStatisticsResponse;
import com.example.chalpuplatform.campaign.repository.CampaignRepository;
import com.example.chalpuplatform.common.exception.CampaignException;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.response.PageResponse;
import com.example.chalpuplatform.customerfeedback.repository.CustomerFeedbackRepository;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CampaignQueryService 테스트")
class CampaignQueryServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private CustomerFeedbackRepository customerFeedbackRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private CampaignDomainService campaignDomainService;

    @InjectMocks
    private CampaignQueryService campaignQueryService;

    private Store store;
    private FoodItem foodItem;
    private Campaign campaign;
    private Campaign activeCampaign;
    private Campaign completedCampaign;

    @BeforeEach
    void setUp() {
        store = Store.builder()
            .id(1L)
            .storeName("테스트 매장")
            .address("서울시 강남구")
            .build();

        foodItem = FoodItem.builder()
            .id(1L)
            .foodName("테스트 음식")
            .store(store)
            .price(new java.math.BigDecimal("10000"))
            .build();

        campaign = Campaign.builder()
            .id(1L)
            .name("테스트 캠페인")
            .description("캠페인 설명")
            .store(store)
            .foodItem(foodItem)
            .targetFeedbackCount(100)
            .startDate(LocalDateTime.now().minusDays(10))
            .endDate(LocalDateTime.now().plusDays(20))
            .status(Campaign.CampaignStatus.ACTIVE)
            .isActive(true)
            .build();

        activeCampaign = Campaign.builder()
            .id(2L)
            .name("활성 캠페인")
            .store(store)
            .foodItem(foodItem)
            .targetFeedbackCount(50)
            .startDate(LocalDateTime.now().minusDays(5))
            .endDate(LocalDateTime.now().plusDays(25))
            .status(Campaign.CampaignStatus.ACTIVE)
            .isActive(true)
            .build();

        completedCampaign = Campaign.builder()
            .id(3L)
            .name("완료된 캠페인")
            .store(store)
            .foodItem(foodItem)
            .targetFeedbackCount(30)
            .startDate(LocalDateTime.now().minusDays(30))
            .endDate(LocalDateTime.now().minusDays(1))
            .status(Campaign.CampaignStatus.COMPLETED)
            .isActive(true)
            .build();
    }

    @Nested
    @DisplayName("캠페인 상세 조회 테스트")
    class GetCampaignByIdTest {

        @Test
        @DisplayName("캠페인 상세 정보를 성공적으로 조회한다")
        void getCampaignById_Success() {
            // given
            given(campaignRepository.findById(1L)).willReturn(Optional.of(campaign));
            given(customerFeedbackRepository.countByFoodItemAndStoreBetweenDates(
                eq(foodItem),
                eq(store),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
            )).willReturn(75L);

            // when
            CampaignDetailResponse response = campaignQueryService.getCampaignById(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("테스트 캠페인");
            assertThat(response.getCurrentFeedbackCount()).isEqualTo(75L);
            assertThat(response.getTargetFeedbackCount()).isEqualTo(100);
            assertThat(response.getProgressRate()).isEqualTo(75.0);
            assertThat(response.getIsActive()).isTrue();

            verify(customerFeedbackRepository).countByFoodItemAndStoreBetweenDates(
                eq(foodItem),
                eq(store),
                eq(campaign.getStartDate()),
                eq(campaign.getEndDate())
            );
        }

        @Test
        @DisplayName("목표 달성 시 자동으로 캠페인을 완료 처리한다")
        void getCampaignById_AutoComplete() {
            // given
            given(campaignRepository.findById(1L)).willReturn(Optional.of(campaign));
            given(customerFeedbackRepository.countByFoodItemAndStoreBetweenDates(
                any(), any(), any(), any()
            )).willReturn(100L);
            given(campaignDomainService.shouldAutoComplete(campaign, 100L)).willReturn(true);

            // when
            CampaignDetailResponse response = campaignQueryService.getCampaignById(1L);

            // then
            assertThat(response.getCurrentFeedbackCount()).isEqualTo(100L);
            assertThat(response.getProgressRate()).isEqualTo(100.0);

            verify(campaignRepository).save(campaign);
            assertThat(campaign.getStatus()).isEqualTo(Campaign.CampaignStatus.COMPLETED);
        }

        @Test
        @DisplayName("존재하지 않는 캠페인 조회 시 예외가 발생한다")
        void getCampaignById_NotFound() {
            // given
            given(campaignRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> campaignQueryService.getCampaignById(999L))
                .isInstanceOf(CampaignException.class)
                .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.CAMPAIGN_NOT_FOUND);

            verify(customerFeedbackRepository, never()).countByFoodItemAndStoreBetweenDates(
                any(), any(), any(), any()
            );
        }
    }

    @Nested
    @DisplayName("매장별 캠페인 목록 조회 테스트")
    class GetCampaignsByStoreTest {

        @Test
        @DisplayName("매장의 캠페인 목록을 페이징하여 조회한다")
        void getCampaignsByStore_Success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<Campaign> campaigns = Arrays.asList(campaign, activeCampaign, completedCampaign);
            Page<Campaign> campaignPage = new PageImpl<>(campaigns, pageable, campaigns.size());

            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(campaignRepository.findByStoreAndIsActiveTrue(store, pageable))
                .willReturn(campaignPage);

            // when
            PageResponse<CampaignResponse> response = campaignQueryService.getCampaignsByStore(1L, pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(3);
            assertThat(response.getTotalElements()).isEqualTo(3);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.getPage()).isEqualTo(0);

            CampaignResponse firstCampaign = response.getContent().get(0);
            assertThat(firstCampaign.getName()).isEqualTo("테스트 캠페인");
            assertThat(firstCampaign.getTargetFeedbackCount()).isEqualTo(100);
        }

        @Test
        @DisplayName("존재하지 않는 매장의 캠페인 목록 조회 시 예외가 발생한다")
        void getCampaignsByStore_StoreNotFound() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            given(storeRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> campaignQueryService.getCampaignsByStore(999L, pageable))
                .isInstanceOf(CampaignException.class)
                .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.STORE_NOT_FOUND);

            verify(campaignRepository, never()).findByStoreAndIsActiveTrue(any(), any());
        }

        @Test
        @DisplayName("캠페인이 없는 매장의 목록 조회 시 빈 페이지를 반환한다")
        void getCampaignsByStore_EmptyResult() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Campaign> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(campaignRepository.findByStoreAndIsActiveTrue(store, pageable))
                .willReturn(emptyPage);

            // when
            PageResponse<CampaignResponse> response = campaignQueryService.getCampaignsByStore(1L, pageable);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0);
            assertThat(response.getTotalPages()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("활성 캠페인 조회 테스트")
    class GetActiveCampaignsByStoreTest {

        @Test
        @DisplayName("매장의 활성 캠페인 목록을 조회한다")
        void getActiveCampaignsByStore_Success() {
            // given
            List<Campaign> activeCampaigns = Arrays.asList(campaign, activeCampaign);

            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(campaignRepository.findActiveCampaignsByStore(eq(store), any(LocalDateTime.class)))
                .willReturn(activeCampaigns);

            // when
            List<CampaignResponse> responses = campaignQueryService.getActiveCampaignsByStore(1L);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getStatus()).isEqualTo("활성");
            assertThat(responses.get(1).getStatus()).isEqualTo("활성");

            verify(campaignRepository).findActiveCampaignsByStore(eq(store), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("활성 캠페인이 없을 때 빈 목록을 반환한다")
        void getActiveCampaignsByStore_EmptyResult() {
            // given
            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(campaignRepository.findActiveCampaignsByStore(eq(store), any(LocalDateTime.class)))
                .willReturn(List.of());

            // when
            List<CampaignResponse> responses = campaignQueryService.getActiveCampaignsByStore(1L);

            // then
            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("캠페인 통계 조회 테스트")
    class GetCampaignStatisticsTest {

        @Test
        @DisplayName("캠페인 통계를 성공적으로 조회한다")
        void getCampaignStatistics_Success() {
            // given
            given(campaignRepository.findById(1L)).willReturn(Optional.of(campaign));
            given(customerFeedbackRepository.countByFoodItemAndStoreBetweenDates(
                any(), any(), any(), any()
            )).willReturn(75L);

            given(customerFeedbackRepository.findAverageSatisfactionForCampaign(
                any(), any(), any(), any()
            )).willReturn(4.5);

            List<Object[]> dailyCounts = Arrays.asList(
                new Object[]{LocalDate.now().minusDays(5), 10L},
                new Object[]{LocalDate.now().minusDays(4), 15L},
                new Object[]{LocalDate.now().minusDays(3), 20L},
                new Object[]{LocalDate.now().minusDays(2), 15L},
                new Object[]{LocalDate.now().minusDays(1), 15L}
            );

            given(customerFeedbackRepository.findDailyFeedbackCounts(
                any(), any(), any(), any()
            )).willReturn(dailyCounts);

            // when
            CampaignStatisticsResponse response = campaignQueryService.getCampaignStatistics(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCampaignId()).isEqualTo(1L);
            assertThat(response.getCampaignName()).isEqualTo("테스트 캠페인");
            assertThat(response.getTotalFeedbackCount()).isEqualTo(75L);
            assertThat(response.getProgressRate()).isEqualTo(75.0);
            assertThat(response.getAverageSatisfaction()).isEqualTo(4.5);
            assertThat(response.getDailyFeedbacks()).hasSize(5);

            // 일별 피드백 누적 수 확인
            assertThat(response.getDailyFeedbacks().get(0).getCount()).isEqualTo(10L);
            assertThat(response.getDailyFeedbacks().get(0).getCumulativeCount()).isEqualTo(10L);
            assertThat(response.getDailyFeedbacks().get(1).getCumulativeCount()).isEqualTo(25L);
            assertThat(response.getDailyFeedbacks().get(4).getCumulativeCount()).isEqualTo(75L);
        }

        @Test
        @DisplayName("피드백이 없는 캠페인의 통계를 조회한다")
        void getCampaignStatistics_NoFeedback() {
            // given
            given(campaignRepository.findById(1L)).willReturn(Optional.of(campaign));
            given(customerFeedbackRepository.countByFoodItemAndStoreBetweenDates(
                any(), any(), any(), any()
            )).willReturn(0L);
            given(customerFeedbackRepository.findAverageSatisfactionForCampaign(
                any(), any(), any(), any()
            )).willReturn(null);
            given(customerFeedbackRepository.findDailyFeedbackCounts(
                any(), any(), any(), any()
            )).willReturn(List.of());

            // when
            CampaignStatisticsResponse response = campaignQueryService.getCampaignStatistics(1L);

            // then
            assertThat(response.getTotalFeedbackCount()).isEqualTo(0L);
            assertThat(response.getProgressRate()).isEqualTo(0.0);
            assertThat(response.getAverageSatisfaction()).isNull();
            assertThat(response.getDailyFeedbacks()).isEmpty();
        }

        @Test
        @DisplayName("종료된 캠페인의 남은 일수는 0이다")
        void getCampaignStatistics_ExpiredCampaign() {
            // given
            given(campaignRepository.findById(3L)).willReturn(Optional.of(completedCampaign));
            given(customerFeedbackRepository.countByFoodItemAndStoreBetweenDates(
                any(), any(), any(), any()
            )).willReturn(30L);

            // when
            CampaignStatisticsResponse response = campaignQueryService.getCampaignStatistics(3L);

            // then
            assertThat(response.getDaysRemaining()).isEqualTo(0);
            assertThat(response.getProgressRate()).isEqualTo(100.0);
        }
    }

    @Nested
    @DisplayName("캠페인 대시보드 조회 테스트")
    class GetDashboardDataTest {

        @Test
        @DisplayName("매장의 캠페인 대시보드 데이터를 조회한다")
        void getDashboardData_Success() {
            // given
            List<Campaign> activeCampaigns = Arrays.asList(campaign, activeCampaign);

            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(campaignRepository.findActiveCampaignsByStore(eq(store), any(LocalDateTime.class)))
                .willReturn(activeCampaigns);
            given(customerFeedbackRepository.countByFoodItemAndStoreBetweenDates(
                any(), any(), any(), any()
            )).willReturn(75L, 40L); // 각 캠페인별 피드백 수

            // when
            Map<String, Object> dashboard = campaignQueryService.getDashboardData(1L);

            // then
            assertThat(dashboard).isNotNull();
            assertThat(dashboard.get("activeCampaignCount")).isEqualTo(2);
            assertThat(dashboard.get("lastUpdated")).isNotNull();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> campaigns = (List<Map<String, Object>>) dashboard.get("campaigns");
            assertThat(campaigns).hasSize(2);

            Map<String, Object> firstCampaign = campaigns.get(0);
            assertThat(firstCampaign.get("campaignId")).isEqualTo(1L);
            assertThat(firstCampaign.get("campaignName")).isEqualTo("테스트 캠페인");
            assertThat(firstCampaign.get("foodItemName")).isEqualTo("테스트 음식");
            assertThat(firstCampaign.get("targetCount")).isEqualTo(100);
            assertThat(firstCampaign.get("currentCount")).isEqualTo(75L);
            assertThat(firstCampaign.get("progressRate")).isEqualTo(75.0);
            assertThat(firstCampaign.get("status")).isEqualTo("활성");
        }

        @Test
        @DisplayName("활성 캠페인이 없는 매장의 대시보드를 조회한다")
        void getDashboardData_NoCampaigns() {
            // given
            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(campaignRepository.findActiveCampaignsByStore(eq(store), any(LocalDateTime.class)))
                .willReturn(List.of());

            // when
            Map<String, Object> dashboard = campaignQueryService.getDashboardData(1L);

            // then
            assertThat(dashboard.get("activeCampaignCount")).isEqualTo(0);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> campaigns = (List<Map<String, Object>>) dashboard.get("campaigns");
            assertThat(campaigns).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 매장의 대시보드 조회 시 예외가 발생한다")
        void getDashboardData_StoreNotFound() {
            // given
            given(storeRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> campaignQueryService.getDashboardData(999L))
                .isInstanceOf(CampaignException.class)
                .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.STORE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("캠페인 진행률 조회 테스트")
    class GetCampaignWithProgressTest {

        @Test
        @DisplayName("캠페인 진행률을 포함한 상세 정보를 조회한다")
        void getCampaignWithProgress_Success() {
            // given
            given(campaignRepository.findById(1L)).willReturn(Optional.of(campaign));
            given(customerFeedbackRepository.countByFoodItemAndStoreBetweenDates(
                any(), any(), any(), any()
            )).willReturn(75L);

            // when
            CampaignDetailResponse response = campaignQueryService.getCampaignWithProgress(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCurrentFeedbackCount()).isEqualTo(75L);
            assertThat(response.getProgressRate()).isEqualTo(75.0);
            assertThat(response.getTargetFeedbackCount()).isEqualTo(100);
        }

        @Test
        @DisplayName("목표를 초과한 캠페인의 진행률은 100%를 넘지 않는다")
        void getCampaignWithProgress_ExceedTarget() {
            // given
            given(campaignRepository.findById(1L)).willReturn(Optional.of(campaign));
            given(customerFeedbackRepository.countByFoodItemAndStoreBetweenDates(
                any(), any(), any(), any()
            )).willReturn(150L); // 목표 초과

            // when
            CampaignDetailResponse response = campaignQueryService.getCampaignWithProgress(1L);

            // then
            assertThat(response.getCurrentFeedbackCount()).isEqualTo(150L);
            assertThat(response.getProgressRate()).isEqualTo(100.0); // 100%를 넘지 않음
        }
    }
}