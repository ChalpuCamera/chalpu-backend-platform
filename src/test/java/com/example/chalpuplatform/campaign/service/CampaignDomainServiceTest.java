package com.example.chalpuplatform.campaign.service;

import com.example.chalpuplatform.campaign.domain.Campaign;
import com.example.chalpuplatform.campaign.repository.CampaignRepository;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.store.domain.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CampaignDomainService 테스트")
class CampaignDomainServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private CampaignDomainService campaignDomainService;

    private Store store;
    private FoodItem foodItem;
    private Campaign campaign;

    @BeforeEach
    void setUp() {
        store = Store.builder()
            .id(1L)
            .storeName("테스트 매장")
            .build();

        foodItem = FoodItem.builder()
            .id(1L)
            .foodName("테스트 음식")
            .store(store)
            .build();

        campaign = Campaign.builder()
            .id(1L)
            .name("테스트 캠페인")
            .store(store)
            .foodItem(foodItem)
            .targetFeedbackCount(50)
            .startDate(LocalDateTime.now().plusDays(1))
            .endDate(LocalDateTime.now().plusDays(30))
            .status(Campaign.CampaignStatus.DRAFT)
            .build();
    }

    @Nested
    @DisplayName("캠페인 생성 검증 테스트")
    class ValidateCampaignCreationTest {

        @Test
        @DisplayName("정상적인 캠페인 생성 요청을 검증한다")
        void validateCampaignCreation_Success() {
            // given
            LocalDateTime startDate = LocalDateTime.now().plusDays(1);
            LocalDateTime endDate = LocalDateTime.now().plusDays(30);

            given(campaignRepository.existsOverlappingCampaign(
                eq(foodItem), eq(startDate), eq(endDate), eq(0L)
            )).willReturn(false);

            // when & then - 예외가 발생하지 않음
            campaignDomainService.validateCampaignCreation(foodItem, startDate, endDate);

            verify(campaignRepository).existsOverlappingCampaign(
                eq(foodItem), eq(startDate), eq(endDate), eq(0L)
            );
        }

        @Test
        @DisplayName("시작일이 종료일보다 늦으면 예외가 발생한다")
        void validateCampaignCreation_InvalidDateRange() {
            // given
            LocalDateTime startDate = LocalDateTime.now().plusDays(30);
            LocalDateTime endDate = LocalDateTime.now().plusDays(1);

            // when & then
            assertThatThrownBy(() ->
                campaignDomainService.validateCampaignCreation(foodItem, startDate, endDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시작일은 종료일보다 이전이어야 합니다");

            verify(campaignRepository, never()).existsOverlappingCampaign(any(), any(), any(), anyLong());
        }

        @Test
        @DisplayName("종료일이 현재 시간보다 이전이면 예외가 발생한다")
        void validateCampaignCreation_EndDateInPast() {
            // given
            LocalDateTime startDate = LocalDateTime.now().minusDays(30);
            LocalDateTime endDate = LocalDateTime.now().minusDays(1);

            // when & then
            assertThatThrownBy(() ->
                campaignDomainService.validateCampaignCreation(foodItem, startDate, endDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종료일은 현재 시간 이후여야 합니다");

            verify(campaignRepository, never()).existsOverlappingCampaign(any(), any(), any(), anyLong());
        }

        @Test
        @DisplayName("중복된 캠페인이 있으면 예외가 발생한다")
        void validateCampaignCreation_OverlappingCampaign() {
            // given
            LocalDateTime startDate = LocalDateTime.now().plusDays(1);
            LocalDateTime endDate = LocalDateTime.now().plusDays(30);

            given(campaignRepository.existsOverlappingCampaign(
                eq(foodItem), eq(startDate), eq(endDate), eq(0L)
            )).willReturn(true);

            // when & then
            assertThatThrownBy(() ->
                campaignDomainService.validateCampaignCreation(foodItem, startDate, endDate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("해당 기간에 이미 진행 중이거나 예정된 캠페인이 있습니다");
        }

        @Test
        @DisplayName("시작일과 종료일이 같아도 검증을 통과한다")
        void validateCampaignCreation_SameDates() {
            // given
            LocalDateTime sameDate = LocalDateTime.now().plusDays(1);

            given(campaignRepository.existsOverlappingCampaign(
                eq(foodItem), eq(sameDate), eq(sameDate), eq(0L)
            )).willReturn(false);

            // when & then - 예외가 발생하지 않음
            campaignDomainService.validateCampaignCreation(foodItem, sameDate, sameDate);

            verify(campaignRepository).existsOverlappingCampaign(
                eq(foodItem), eq(sameDate), eq(sameDate), eq(0L)
            );
        }
    }

    @Nested
    @DisplayName("캠페인 수정 검증 테스트")
    class ValidateCampaignUpdateTest {

        @Test
        @DisplayName("수정 가능한 상태의 캠페인 수정을 검증한다")
        void validateCampaignUpdate_Success() {
            // given
            LocalDateTime startDate = LocalDateTime.now().plusDays(2);
            LocalDateTime endDate = LocalDateTime.now().plusDays(60);

            given(campaignRepository.existsOverlappingCampaign(
                eq(foodItem), eq(startDate), eq(endDate), eq(1L)
            )).willReturn(false);

            // when & then - 예외가 발생하지 않음
            campaignDomainService.validateCampaignUpdate(campaign, startDate, endDate);

            verify(campaignRepository).existsOverlappingCampaign(
                eq(foodItem), eq(startDate), eq(endDate), eq(1L)
            );
        }

        @Test
        @DisplayName("수정 불가능한 상태의 캠페인 수정 시 예외가 발생한다")
        void validateCampaignUpdate_CannotModify() {
            // given
            Campaign activeCampaign = Campaign.builder()
                .id(1L)
                .name("활성 캠페인")
                .store(store)
                .foodItem(foodItem)
                .status(Campaign.CampaignStatus.ACTIVE)
                .build();

            LocalDateTime startDate = LocalDateTime.now().plusDays(2);
            LocalDateTime endDate = LocalDateTime.now().plusDays(60);

            // when & then
            assertThatThrownBy(() ->
                campaignDomainService.validateCampaignUpdate(activeCampaign, startDate, endDate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("활성 또는 완료된 캠페인은 수정할 수 없습니다");

            verify(campaignRepository, never()).existsOverlappingCampaign(any(), any(), any(), anyLong());
        }

        @Test
        @DisplayName("완료된 캠페인 수정 시 예외가 발생한다")
        void validateCampaignUpdate_CompletedCampaign() {
            // given
            Campaign completedCampaign = Campaign.builder()
                .id(1L)
                .name("완료된 캠페인")
                .store(store)
                .foodItem(foodItem)
                .status(Campaign.CampaignStatus.COMPLETED)
                .build();

            LocalDateTime startDate = LocalDateTime.now().plusDays(2);
            LocalDateTime endDate = LocalDateTime.now().plusDays(60);

            // when & then
            assertThatThrownBy(() ->
                campaignDomainService.validateCampaignUpdate(completedCampaign, startDate, endDate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("활성 또는 완료된 캠페인은 수정할 수 없습니다");
        }

        @Test
        @DisplayName("시작일이 종료일보다 늦으면 예외가 발생한다")
        void validateCampaignUpdate_InvalidDateRange() {
            // given
            LocalDateTime startDate = LocalDateTime.now().plusDays(60);
            LocalDateTime endDate = LocalDateTime.now().plusDays(2);

            // when & then
            assertThatThrownBy(() ->
                campaignDomainService.validateCampaignUpdate(campaign, startDate, endDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시작일은 종료일보다 이전이어야 합니다");
        }

        @Test
        @DisplayName("자기 자신을 제외한 중복 캠페인이 있으면 예외가 발생한다")
        void validateCampaignUpdate_OverlappingCampaign() {
            // given
            LocalDateTime startDate = LocalDateTime.now().plusDays(2);
            LocalDateTime endDate = LocalDateTime.now().plusDays(60);

            given(campaignRepository.existsOverlappingCampaign(
                eq(foodItem), eq(startDate), eq(endDate), eq(1L)
            )).willReturn(true);

            // when & then
            assertThatThrownBy(() ->
                campaignDomainService.validateCampaignUpdate(campaign, startDate, endDate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("해당 기간에 이미 진행 중이거나 예정된 캠페인이 있습니다");
        }
    }

    @Nested
    @DisplayName("목표 피드백 수 검증 테스트")
    class ValidateTargetFeedbackCountTest {

        @Test
        @DisplayName("유효한 목표 피드백 수를 검증한다")
        void validateTargetFeedbackCount_ValidCount() {
            // when & then - 예외가 발생하지 않음
            campaignDomainService.validateTargetFeedbackCount(1);
            campaignDomainService.validateTargetFeedbackCount(50);
            campaignDomainService.validateTargetFeedbackCount(100);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10, -100})
        @DisplayName("목표 피드백 수가 최소값 미만이면 예외가 발생한다")
        void validateTargetFeedbackCount_BelowMinimum(int count) {
            // when & then
            assertThatThrownBy(() ->
                campaignDomainService.validateTargetFeedbackCount(count))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("목표 피드백 수는 1개 이상이어야 합니다");
        }

        @ParameterizedTest
        @ValueSource(ints = {101, 200, 1000, 10000})
        @DisplayName("목표 피드백 수가 최대값 초과하면 예외가 발생한다")
        void validateTargetFeedbackCount_AboveMaximum(int count) {
            // when & then
            assertThatThrownBy(() ->
                campaignDomainService.validateTargetFeedbackCount(count))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("목표 피드백 수는 100개를 초과할 수 없습니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("목표 피드백 수가 null이면 예외가 발생한다")
        void validateTargetFeedbackCount_NullValue(Integer count) {
            // when & then
            assertThatThrownBy(() ->
                campaignDomainService.validateTargetFeedbackCount(count))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("목표 피드백 수는 1개 이상이어야 합니다");
        }

        @Test
        @DisplayName("경계값 테스트 - 최소값")
        void validateTargetFeedbackCount_BoundaryMin() {
            // when & then
            campaignDomainService.validateTargetFeedbackCount(1); // 정상

            assertThatThrownBy(() ->
                campaignDomainService.validateTargetFeedbackCount(0))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("경계값 테스트 - 최대값")
        void validateTargetFeedbackCount_BoundaryMax() {
            // when & then
            campaignDomainService.validateTargetFeedbackCount(100); // 정상

            assertThatThrownBy(() ->
                campaignDomainService.validateTargetFeedbackCount(101))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("캠페인 자동 완료 테스트")
    class CampaignAutoCompleteTest {

        @Test
        @DisplayName("목표 달성한 활성 캠페인은 자동 완료 대상이다")
        void shouldAutoComplete_ActiveAndTargetReached() {
            // given
            Campaign activeCampaign = Campaign.builder()
                .id(1L)
                .status(Campaign.CampaignStatus.ACTIVE)
                .targetFeedbackCount(50)
                .build();

            // when
            boolean result = campaignDomainService.shouldAutoComplete(activeCampaign, 50);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("목표를 초과한 활성 캠페인도 자동 완료 대상이다")
        void shouldAutoComplete_ExceedTarget() {
            // given
            Campaign activeCampaign = Campaign.builder()
                .id(1L)
                .status(Campaign.CampaignStatus.ACTIVE)
                .targetFeedbackCount(50)
                .build();

            // when
            boolean result = campaignDomainService.shouldAutoComplete(activeCampaign, 100);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("목표 미달성 활성 캠페인은 자동 완료 대상이 아니다")
        void shouldAutoComplete_NotReached() {
            // given
            Campaign activeCampaign = Campaign.builder()
                .id(1L)
                .status(Campaign.CampaignStatus.ACTIVE)
                .targetFeedbackCount(50)
                .build();

            // when
            boolean result = campaignDomainService.shouldAutoComplete(activeCampaign, 49);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("비활성 상태 캠페인은 목표 달성해도 자동 완료 대상이 아니다")
        void shouldAutoComplete_NotActive() {
            // given
            Campaign draftCampaign = Campaign.builder()
                .id(1L)
                .status(Campaign.CampaignStatus.DRAFT)
                .targetFeedbackCount(50)
                .build();

            // when
            boolean result = campaignDomainService.shouldAutoComplete(draftCampaign, 50);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("이미 완료된 캠페인은 자동 완료 대상이 아니다")
        void shouldAutoComplete_AlreadyCompleted() {
            // given
            Campaign completedCampaign = Campaign.builder()
                .id(1L)
                .status(Campaign.CampaignStatus.COMPLETED)
                .targetFeedbackCount(50)
                .build();

            // when
            boolean result = campaignDomainService.shouldAutoComplete(completedCampaign, 50);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("캠페인 완료 처리 테스트")
    class ProcessCampaignCompletionTest {

        @Test
        @DisplayName("목표 달성한 활성 캠페인을 완료 처리한다")
        void processCampaignCompletion_Success() {
            // given
            Campaign activeCampaign = spy(Campaign.builder()
                .id(1L)
                .name("테스트 캠페인")
                .status(Campaign.CampaignStatus.ACTIVE)
                .targetFeedbackCount(50)
                .build());

            // when
            campaignDomainService.processCampaignCompletion(activeCampaign, 50);

            // then
            verify(activeCampaign).complete();
        }

        @Test
        @DisplayName("목표 미달성 캠페인은 완료 처리하지 않는다")
        void processCampaignCompletion_NotReached() {
            // given
            Campaign activeCampaign = spy(Campaign.builder()
                .id(1L)
                .status(Campaign.CampaignStatus.ACTIVE)
                .targetFeedbackCount(50)
                .build());

            // when
            campaignDomainService.processCampaignCompletion(activeCampaign, 30);

            // then
            verify(activeCampaign, never()).complete();
        }

        @Test
        @DisplayName("비활성 캠페인은 목표 달성해도 완료 처리하지 않는다")
        void processCampaignCompletion_NotActive() {
            // given
            Campaign pausedCampaign = spy(Campaign.builder()
                .id(1L)
                .status(Campaign.CampaignStatus.PAUSED)
                .targetFeedbackCount(50)
                .build());

            // when
            campaignDomainService.processCampaignCompletion(pausedCampaign, 50);

            // then
            verify(pausedCampaign, never()).complete();
        }
    }

    @Nested
    @DisplayName("캠페인 만료 처리 테스트")
    class ProcessCampaignExpirationTest {

        @Test
        @DisplayName("만료된 활성 캠페인을 만료 상태로 변경한다")
        void processCampaignExpiration_ActiveExpired() {
            // given
            Campaign expiredCampaign = spy(Campaign.builder()
                .id(1L)
                .name("만료된 캠페인")
                .status(Campaign.CampaignStatus.ACTIVE)
                .startDate(LocalDateTime.now().minusDays(30))
                .endDate(LocalDateTime.now().minusDays(1))
                .build());

            // when
            campaignDomainService.processCampaignExpiration(expiredCampaign);

            // then
            verify(expiredCampaign).expire();
        }

        @Test
        @DisplayName("만료된 일시중지 캠페인을 만료 상태로 변경한다")
        void processCampaignExpiration_PausedExpired() {
            // given
            Campaign pausedExpiredCampaign = spy(Campaign.builder()
                .id(1L)
                .status(Campaign.CampaignStatus.PAUSED)
                .startDate(LocalDateTime.now().minusDays(30))
                .endDate(LocalDateTime.now().minusDays(1))
                .build());

            // when
            campaignDomainService.processCampaignExpiration(pausedExpiredCampaign);

            // then
            verify(pausedExpiredCampaign).expire();
        }

        @Test
        @DisplayName("이미 완료된 캠페인은 만료 처리하지 않는다")
        void processCampaignExpiration_AlreadyCompleted() {
            // given
            Campaign completedCampaign = spy(Campaign.builder()
                .id(1L)
                .status(Campaign.CampaignStatus.COMPLETED)
                .startDate(LocalDateTime.now().minusDays(30))
                .endDate(LocalDateTime.now().minusDays(1))
                .build());

            // when
            campaignDomainService.processCampaignExpiration(completedCampaign);

            // then
            verify(completedCampaign, never()).expire();
        }

        @Test
        @DisplayName("아직 만료되지 않은 캠페인은 만료 처리하지 않는다")
        void processCampaignExpiration_NotExpired() {
            // given
            Campaign activeCampaign = spy(Campaign.builder()
                .id(1L)
                .status(Campaign.CampaignStatus.ACTIVE)
                .startDate(LocalDateTime.now().minusDays(10))
                .endDate(LocalDateTime.now().plusDays(10))
                .build());

            // when
            campaignDomainService.processCampaignExpiration(activeCampaign);

            // then
            verify(activeCampaign, never()).expire();
        }
    }
}