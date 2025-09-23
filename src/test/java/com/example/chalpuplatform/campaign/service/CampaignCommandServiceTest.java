package com.example.chalpuplatform.campaign.service;

import com.example.chalpuplatform.campaign.domain.Campaign;
import com.example.chalpuplatform.campaign.dto.CreateCampaignRequest;
import com.example.chalpuplatform.campaign.dto.UpdateCampaignRequest;
import com.example.chalpuplatform.campaign.repository.CampaignRepository;
import com.example.chalpuplatform.common.exception.CampaignException;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.fooditem.repository.FoodItemRepository;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.domain.StoreRoleType;
import com.example.chalpuplatform.store.domain.UserStoreRole;
import com.example.chalpuplatform.store.repository.StoreRepository;
import com.example.chalpuplatform.store.repository.UserStoreRoleRepository;
import com.example.chalpuplatform.user.domain.Role;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CampaignCommandService 테스트")
class CampaignCommandServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private CampaignDomainService campaignDomainService;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private FoodItemRepository foodItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStoreRoleRepository userStoreRoleRepository;

    @InjectMocks
    private CampaignCommandService campaignCommandService;

    private User user;
    private Store store;
    private FoodItem foodItem;
    private Campaign campaign;
    private UserStoreRole userStoreRole;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .email("owner@test.com")
            .role(Role.ROLE_OWNER)
            .build();

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

        userStoreRole = UserStoreRole.builder()
            .user(user)
            .store(store)
            .roleType(StoreRoleType.OWNER)
            .build();
    }

    @Nested
    @DisplayName("캠페인 생성 테스트")
    class CreateCampaignTest {

        private CreateCampaignRequest request;

        @BeforeEach
        void setUp() {
            request = CreateCampaignRequest.builder()
                .name("새 캠페인")
                .description("캠페인 설명")
                .storeId(1L)
                .foodItemId(1L)
                .targetFeedbackCount(50)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .build();
        }

        @Test
        @DisplayName("정상적으로 캠페인을 생성한다")
        void createCampaign_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrueWithoutJoin(1L, 1L))
                .willReturn(Optional.of(userStoreRole));
            given(foodItemRepository.findById(1L)).willReturn(Optional.of(foodItem));
            given(campaignRepository.save(any(Campaign.class))).willReturn(campaign);

            // when
            Long campaignId = campaignCommandService.createCampaign(request, 1L);

            // then
            assertThat(campaignId).isEqualTo(1L);

            ArgumentCaptor<Campaign> campaignCaptor = ArgumentCaptor.forClass(Campaign.class);
            verify(campaignRepository).save(campaignCaptor.capture());

            Campaign savedCampaign = campaignCaptor.getValue();
            assertThat(savedCampaign.getName()).isEqualTo("새 캠페인");
            assertThat(savedCampaign.getStore()).isEqualTo(store);
            assertThat(savedCampaign.getFoodItem()).isEqualTo(foodItem);
            assertThat(savedCampaign.getTargetFeedbackCount()).isEqualTo(50);

            verify(campaignDomainService).validateTargetFeedbackCount(50);
            verify(campaignDomainService).validateCampaignCreation(
                eq(foodItem),
                eq(request.getStartDate()),
                eq(request.getEndDate())
            );
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 캠페인 생성 시 예외가 발생한다")
        void createCampaign_UserNotFound() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> campaignCommandService.createCampaign(request, 1L))
                .isInstanceOf(CampaignException.class)
                .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.USER_NOT_FOUND);

            verify(storeRepository, never()).findById(anyLong());
            verify(campaignRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 매장으로 캠페인 생성 시 예외가 발생한다")
        void createCampaign_StoreNotFound() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(storeRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> campaignCommandService.createCampaign(request, 1L))
                .isInstanceOf(CampaignException.class)
                .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.STORE_NOT_FOUND);

            verify(foodItemRepository, never()).findById(anyLong());
            verify(campaignRepository, never()).save(any());
        }

        @Test
        @DisplayName("매장 접근 권한이 없을 때 예외가 발생한다")
        void createCampaign_StoreAccessDenied() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrueWithoutJoin(1L, 1L))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> campaignCommandService.createCampaign(request, 1L))
                .isInstanceOf(CampaignException.class)
                .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.STORE_ACCESS_DENIED);

            verify(foodItemRepository, never()).findById(anyLong());
            verify(campaignRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 음식으로 캠페인 생성 시 예외가 발생한다")
        void createCampaign_FoodItemNotFound() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrueWithoutJoin(1L, 1L))
                .willReturn(Optional.of(userStoreRole));
            given(foodItemRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> campaignCommandService.createCampaign(request, 1L))
                .isInstanceOf(CampaignException.class)
                .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.FOOD_ITEM_NOT_FOUND);

            verify(campaignRepository, never()).save(any());
        }

        @Test
        @DisplayName("다른 매장의 음식으로 캠페인 생성 시 예외가 발생한다")
        void createCampaign_FoodItemNotBelongToStore() {
            // given
            Store otherStore = Store.builder()
                .id(2L)
                .storeName("다른 매장")
                .build();

            FoodItem wrongFoodItem = FoodItem.builder()
                .id(1L)
                .foodName("다른 매장 음식")
                .store(otherStore)
                .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrueWithoutJoin(1L, 1L))
                .willReturn(Optional.of(userStoreRole));
            given(foodItemRepository.findById(1L)).willReturn(Optional.of(wrongFoodItem));

            // when & then
            assertThatThrownBy(() -> campaignCommandService.createCampaign(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 음식은 선택한 매장의 메뉴가 아닙니다");

            verify(campaignRepository, never()).save(any());
        }

        @Test
        @DisplayName("도메인 검증 실패 시 예외가 발생한다")
        void createCampaign_DomainValidationFailed() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrueWithoutJoin(1L, 1L))
                .willReturn(Optional.of(userStoreRole));
            given(foodItemRepository.findById(1L)).willReturn(Optional.of(foodItem));

            doThrow(new IllegalArgumentException("목표 피드백 수는 1개 이상이어야 합니다"))
                .when(campaignDomainService).validateTargetFeedbackCount(any());

            // when & then
            assertThatThrownBy(() -> campaignCommandService.createCampaign(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("목표 피드백 수는 1개 이상이어야 합니다");

            verify(campaignRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("캠페인 수정 테스트")
    class UpdateCampaignTest {

        private UpdateCampaignRequest request;

        @BeforeEach
        void setUp() {
            request = UpdateCampaignRequest.builder()
                .name("수정된 캠페인")
                .description("수정된 설명")
                .targetFeedbackCount(100)
                .startDate(LocalDateTime.now().plusDays(2))
                .endDate(LocalDateTime.now().plusDays(60))
                .build();
        }

        @Test
        @DisplayName("정상적으로 캠페인을 수정한다")
        void updateCampaign_Success() {
            // given
            given(campaignRepository.findById(1L)).willReturn(Optional.of(campaign));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(campaignRepository.save(any(Campaign.class))).willReturn(campaign);

            // when
            campaignCommandService.updateCampaign(1L, request, 1L);

            // then
            verify(campaignDomainService).validateTargetFeedbackCount(100);
            verify(campaignDomainService).validateCampaignUpdate(
                eq(campaign),
                eq(request.getStartDate()),
                eq(request.getEndDate())
            );
            verify(campaignRepository).save(campaign);
        }

        @Test
        @DisplayName("존재하지 않는 캠페인 수정 시 예외가 발생한다")
        void updateCampaign_CampaignNotFound() {
            // given
            given(campaignRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> campaignCommandService.updateCampaign(1L, request, 1L))
                .isInstanceOf(CampaignException.class)
                .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.CAMPAIGN_NOT_FOUND);

            verify(campaignRepository, never()).save(any());
        }

        @Test
        @DisplayName("권한이 없는 사용자가 캠페인 수정 시 예외가 발생한다")
        void updateCampaign_UnauthorizedAccess() {
            // given
            User nonOwnerUser = User.builder()
                .id(2L)
                .email("user@test.com")
                .role(Role.ROLE_ADMIN)
                .build();

            given(campaignRepository.findById(1L)).willReturn(Optional.of(campaign));
            given(userRepository.findById(2L)).willReturn(Optional.of(nonOwnerUser));

            // when & then
            assertThatThrownBy(() -> campaignCommandService.updateCampaign(1L, request, 2L))
                .isInstanceOf(CampaignException.class)
                .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.UNAUTHORIZED_ACCESS);

            verify(campaignRepository, never()).save(any());
        }

        @Test
        @DisplayName("활성 상태의 캠페인 수정 시 예외가 발생한다")
        void updateCampaign_ActiveCampaignCannotBeModified() {
            // given
            Campaign activeCampaign = Campaign.builder()
                .id(1L)
                .name("활성 캠페인")
                .store(store)
                .foodItem(foodItem)
                .status(Campaign.CampaignStatus.ACTIVE)
                .targetFeedbackCount(50)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .build();

            given(campaignRepository.findById(1L)).willReturn(Optional.of(activeCampaign));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            doThrow(new IllegalStateException("활성 또는 완료된 캠페인은 수정할 수 없습니다"))
                .when(campaignDomainService).validateCampaignUpdate(any(), any(), any());

            // when & then
            assertThatThrownBy(() -> campaignCommandService.updateCampaign(1L, request, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("활성 또는 완료된 캠페인은 수정할 수 없습니다");

            verify(campaignRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("캠페인 삭제 테스트")
    class DeleteCampaignTest {

        @Test
        @DisplayName("정상적으로 캠페인을 삭제한다")
        void deleteCampaign_Success() {
            // given
            given(campaignRepository.findById(1L)).willReturn(Optional.of(campaign));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(campaignRepository.save(any(Campaign.class))).willReturn(campaign);

            // when
            campaignCommandService.deleteCampaign(1L, 1L);

            // then
            ArgumentCaptor<Campaign> campaignCaptor = ArgumentCaptor.forClass(Campaign.class);
            verify(campaignRepository).save(campaignCaptor.capture());

            Campaign deletedCampaign = campaignCaptor.getValue();
            assertThat(deletedCampaign.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("활성 상태의 캠페인 삭제 시 예외가 발생한다")
        void deleteCampaign_ActiveCampaignCannotBeDeleted() {
            // given
            Campaign activeCampaign = Campaign.builder()
                .id(1L)
                .name("활성 캠페인")
                .store(store)
                .foodItem(foodItem)
                .status(Campaign.CampaignStatus.ACTIVE)
                .build();

            given(campaignRepository.findById(1L)).willReturn(Optional.of(activeCampaign));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> campaignCommandService.deleteCampaign(1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("활성 상태의 캠페인은 삭제할 수 없습니다");

            verify(campaignRepository, never()).save(any());
        }

        @Test
        @DisplayName("권한이 없는 사용자가 캠페인 삭제 시 예외가 발생한다")
        void deleteCampaign_UnauthorizedAccess() {
            // given
            User nonOwnerUser = User.builder()
                .id(2L)
                .email("user@test.com")
                .role(Role.ROLE_CUSTOMER)
                .build();

            given(campaignRepository.findById(1L)).willReturn(Optional.of(campaign));
            given(userRepository.findById(2L)).willReturn(Optional.of(nonOwnerUser));

            // when & then
            assertThatThrownBy(() -> campaignCommandService.deleteCampaign(1L, 2L))
                .isInstanceOf(CampaignException.class)
                .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.UNAUTHORIZED_ACCESS);

            verify(campaignRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("캠페인 상태 변경 테스트")
    class ChangeCampaignStatusTest {

        @Test
        @DisplayName("캠페인을 활성 상태로 변경한다")
        void changeCampaignStatus_ToActive() {
            // given
            given(campaignRepository.findById(1L)).willReturn(Optional.of(campaign));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(campaignRepository.save(any(Campaign.class))).willReturn(campaign);

            // when
            campaignCommandService.changeCampaignStatus(1L, Campaign.CampaignStatus.ACTIVE, 1L);

            // then
            ArgumentCaptor<Campaign> campaignCaptor = ArgumentCaptor.forClass(Campaign.class);
            verify(campaignRepository).save(campaignCaptor.capture());

            Campaign activatedCampaign = campaignCaptor.getValue();
            assertThat(activatedCampaign.getStatus()).isEqualTo(Campaign.CampaignStatus.ACTIVE);
        }

        @Test
        @DisplayName("캠페인을 일시중지 상태로 변경한다")
        void changeCampaignStatus_ToPaused() {
            // given
            Campaign activeCampaign = Campaign.builder()
                .id(1L)
                .name("활성 캠페인")
                .store(store)
                .foodItem(foodItem)
                .status(Campaign.CampaignStatus.ACTIVE)
                .targetFeedbackCount(50)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .build();

            given(campaignRepository.findById(1L)).willReturn(Optional.of(activeCampaign));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(campaignRepository.save(any(Campaign.class))).willReturn(activeCampaign);

            // when
            campaignCommandService.changeCampaignStatus(1L, Campaign.CampaignStatus.PAUSED, 1L);

            // then
            verify(campaignRepository).save(activeCampaign);
            assertThat(activeCampaign.getStatus()).isEqualTo(Campaign.CampaignStatus.PAUSED);
        }

        @Test
        @DisplayName("캠페인을 완료 상태로 변경한다")
        void changeCampaignStatus_ToCompleted() {
            // given
            Campaign activeCampaign = Campaign.builder()
                .id(1L)
                .name("활성 캠페인")
                .store(store)
                .foodItem(foodItem)
                .status(Campaign.CampaignStatus.ACTIVE)
                .targetFeedbackCount(50)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .build();

            given(campaignRepository.findById(1L)).willReturn(Optional.of(activeCampaign));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(campaignRepository.save(any(Campaign.class))).willReturn(activeCampaign);

            // when
            campaignCommandService.changeCampaignStatus(1L, Campaign.CampaignStatus.COMPLETED, 1L);

            // then
            verify(campaignRepository).save(activeCampaign);
            assertThat(activeCampaign.getStatus()).isEqualTo(Campaign.CampaignStatus.COMPLETED);
        }

        @Test
        @DisplayName("유효하지 않은 상태로 변경 시 예외가 발생한다")
        void changeCampaignStatus_InvalidStatus() {
            // given
            given(campaignRepository.findById(1L)).willReturn(Optional.of(campaign));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() ->
                campaignCommandService.changeCampaignStatus(1L, Campaign.CampaignStatus.DRAFT, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 상태입니다: DRAFT");

            verify(campaignRepository, never()).save(any());
        }

        @Test
        @DisplayName("권한이 없는 사용자가 상태 변경 시 예외가 발생한다")
        void changeCampaignStatus_UnauthorizedAccess() {
            // given
            User nonOwnerUser = User.builder()
                .id(2L)
                .email("user@test.com")
                .role(Role.ROLE_ADMIN)
                .build();

            given(campaignRepository.findById(1L)).willReturn(Optional.of(campaign));
            given(userRepository.findById(2L)).willReturn(Optional.of(nonOwnerUser));

            // when & then
            assertThatThrownBy(() ->
                campaignCommandService.changeCampaignStatus(1L, Campaign.CampaignStatus.ACTIVE, 2L))
                .isInstanceOf(CampaignException.class)
                .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.UNAUTHORIZED_ACCESS);

            verify(campaignRepository, never()).save(any());
        }
    }
}