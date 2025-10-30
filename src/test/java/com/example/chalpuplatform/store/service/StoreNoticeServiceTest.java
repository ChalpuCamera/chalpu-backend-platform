package com.example.chalpuplatform.store.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.StoreException;
import com.example.chalpuplatform.common.response.PageResponse;
import com.example.chalpuplatform.store.domain.StoreNotice;
import com.example.chalpuplatform.store.domain.UserStoreRole;
import com.example.chalpuplatform.store.dto.CreateStoreNoticeRequest;
import com.example.chalpuplatform.store.dto.StoreNoticeDeleteDto;
import com.example.chalpuplatform.store.dto.StoreNoticeResponse;
import com.example.chalpuplatform.store.dto.UpdateStoreNoticeRequest;
import com.example.chalpuplatform.store.repository.StoreNoticeRepository;
import com.example.chalpuplatform.store.repository.UserStoreRoleRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreNoticeService 테스트")
class StoreNoticeServiceTest {

    @Mock
    private StoreNoticeRepository storeNoticeRepository;

    @Mock
    private UserStoreRoleRepository userStoreRoleRepository;

    @InjectMocks
    private StoreNoticeService storeNoticeService;

    private StoreNotice notice;
    private CreateStoreNoticeRequest createRequest;
    private UpdateStoreNoticeRequest updateRequest;
    private Pageable pageable;
    private Long userId = 1L;
    private Long storeId = 1L;

    @BeforeEach
    void setUp() {
        notice = StoreNotice.builder()
                .storeId(storeId)
                .title("설 연휴 휴무 안내")
                .body("2024년 2월 9일부터 2월 12일까지 휴무입니다.")
                .build();

        createRequest = new CreateStoreNoticeRequest(
                "설 연휴 휴무 안내",
                "2024년 2월 9일부터 2월 12일까지 휴무입니다."
        );

        updateRequest = new UpdateStoreNoticeRequest(
                "추석 연휴 휴무 안내",
                "2024년 9월 16일부터 9월 18일까지 휴무입니다."
        );

        pageable = PageRequest.of(0, 20);
    }

    @Nested
    @DisplayName("공지사항 생성 테스트")
    class CreateNoticeTest {

        @Test
        @DisplayName("공지사항을 성공적으로 생성한다")
        void createNotice_Success() {
            // given
            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId))
                    .willReturn(Optional.of(mock(UserStoreRole.class)));
            given(storeNoticeRepository.save(any(StoreNotice.class)))
                    .willReturn(notice);

            // when
            StoreNoticeResponse response = storeNoticeService.createNotice(storeId, createRequest, userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStoreId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("설 연휴 휴무 안내");
            assertThat(response.getBody()).isEqualTo("2024년 2월 9일부터 2월 12일까지 휴무입니다.");
            verify(userStoreRoleRepository).findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId);
            verify(storeNoticeRepository).save(any(StoreNotice.class));
        }

        @Test
        @DisplayName("사용자가 가게 권한이 없으면 예외가 발생한다")
        void createNotice_UserHasNoPermission_ThrowsException() {
            // given
            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(userId, 999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> storeNoticeService.createNotice(999L, createRequest, userId))
                    .isInstanceOf(StoreException.class)
                    .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.STORE_NOT_FOUND);

            verify(userStoreRoleRepository).findByUserIdAndStoreIdAndIsActiveTrue(userId, 999L);
            verify(storeNoticeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("공지사항 조회 테스트")
    class GetNoticesTest {

        @Test
        @DisplayName("공지사항 목록을 성공적으로 조회한다")
        void getNotices_Success() {
            // given
            StoreNotice notice1 = StoreNotice.builder()
                    .storeId(storeId)
                    .title("공지1")
                    .body("내용1")
                    .build();
            StoreNotice notice2 = StoreNotice.builder()
                    .storeId(storeId)
                    .title("공지2")
                    .body("내용2")
                    .build();

            Page<StoreNotice> noticePage = new PageImpl<>(List.of(notice1, notice2), pageable, 2);

            given(storeNoticeRepository.findByStoreId(storeId, pageable))
                    .willReturn(noticePage);

            // when
            PageResponse<StoreNoticeResponse> response = storeNoticeService.getNotices(storeId, pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getTotalElements()).isEqualTo(2);
            verify(storeNoticeRepository).findByStoreId(storeId, pageable);
        }

        @Test
        @DisplayName("공지사항이 없으면 빈 페이지를 반환한다")
        void getNotices_EmptyResult_ReturnsEmptyPage() {
            // given
            Page<StoreNotice> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            given(storeNoticeRepository.findByStoreId(storeId, pageable))
                    .willReturn(emptyPage);

            // when
            PageResponse<StoreNoticeResponse> response = storeNoticeService.getNotices(storeId, pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("공지사항 수정 테스트")
    class UpdateNoticeTest {

        @Test
        @DisplayName("공지사항을 성공적으로 수정한다")
        void updateNotice_Success() {
            // given
            given(storeNoticeRepository.findById(1L))
                    .willReturn(Optional.of(notice));
            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId))
                    .willReturn(Optional.of(mock(UserStoreRole.class)));

            // when
            StoreNoticeResponse response = storeNoticeService.updateNotice(1L, updateRequest, userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("추석 연휴 휴무 안내");
            assertThat(response.getBody()).isEqualTo("2024년 9월 16일부터 9월 18일까지 휴무입니다.");
            verify(storeNoticeRepository).findById(1L);
            verify(userStoreRoleRepository).findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId);
        }

        @Test
        @DisplayName("공지사항을 찾을 수 없으면 예외가 발생한다")
        void updateNotice_NoticeNotFound_ThrowsException() {
            // given
            given(storeNoticeRepository.findById(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> storeNoticeService.updateNotice(999L, updateRequest, userId))
                    .isInstanceOf(StoreException.class)
                    .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.STORE_NOTICE_NOT_FOUND);

            verify(storeNoticeRepository).findById(999L);
        }

        @Test
        @DisplayName("사용자가 가게 권한이 없으면 예외가 발생한다")
        void updateNotice_UserHasNoPermission_ThrowsException() {
            // given
            given(storeNoticeRepository.findById(1L))
                    .willReturn(Optional.of(notice));
            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> storeNoticeService.updateNotice(1L, updateRequest, userId))
                    .isInstanceOf(StoreException.class)
                    .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.STORE_NOT_FOUND);

            verify(storeNoticeRepository).findById(1L);
            verify(userStoreRoleRepository).findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId);
        }
    }

    @Nested
    @DisplayName("공지사항 벌크 삭제 테스트")
    class DeleteNoticeTest {

        @Test
        @DisplayName("여러 공지사항을 성공적으로 삭제한다")
        void deleteNotice_BulkDelete_Success() {
            // given
            StoreNotice notice1 = StoreNotice.builder()
                    .storeId(storeId)
                    .title("공지1")
                    .body("내용1")
                    .build();
            StoreNotice notice2 = StoreNotice.builder()
                    .storeId(storeId)
                    .title("공지2")
                    .body("내용2")
                    .build();

            List<Long> deleteIds = List.of(1L, 2L);
            StoreNoticeDeleteDto deleteDto = new StoreNoticeDeleteDto(deleteIds);

            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId))
                    .willReturn(Optional.of(mock(UserStoreRole.class)));
            given(storeNoticeRepository.findAllById(deleteIds))
                    .willReturn(List.of(notice1, notice2));

            // when
            storeNoticeService.deleteNotice(deleteDto, storeId, userId);

            // then
            verify(userStoreRoleRepository).findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId);
            verify(storeNoticeRepository).findAllById(deleteIds);
            verify(storeNoticeRepository).deleteAllById(deleteIds);
        }

        @Test
        @DisplayName("일부 공지사항을 찾을 수 없으면 예외가 발생한다")
        void deleteNotice_SomeNoticesNotFound_ThrowsException() {
            // given
            StoreNotice notice1 = StoreNotice.builder()
                    .storeId(storeId)
                    .title("공지1")
                    .body("내용1")
                    .build();

            List<Long> deleteIds = List.of(1L, 999L);
            StoreNoticeDeleteDto deleteDto = new StoreNoticeDeleteDto(deleteIds);

            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId))
                    .willReturn(Optional.of(mock(UserStoreRole.class)));
            given(storeNoticeRepository.findAllById(deleteIds))
                    .willReturn(List.of(notice1));

            // when & then
            assertThatThrownBy(() -> storeNoticeService.deleteNotice(deleteDto, storeId, userId))
                    .isInstanceOf(StoreException.class)
                    .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.STORE_NOTICE_NOT_FOUND);

            verify(userStoreRoleRepository).findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId);
            verify(storeNoticeRepository).findAllById(deleteIds);
            verify(storeNoticeRepository, never()).deleteAllById(any());
        }

        @Test
        @DisplayName("삭제할 ID 목록이 비어있으면 예외가 발생한다")
        void deleteNotice_EmptyIdList_ThrowsException() {
            // given
            StoreNoticeDeleteDto deleteDto = new StoreNoticeDeleteDto(List.of());

            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId))
                    .willReturn(Optional.of(mock(UserStoreRole.class)));

            // when & then
            assertThatThrownBy(() -> storeNoticeService.deleteNotice(deleteDto, storeId, userId))
                    .isInstanceOf(StoreException.class)
                    .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.INVALID_PARAMETER);

            verify(userStoreRoleRepository).findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId);
            verify(storeNoticeRepository, never()).findAllById(any());
            verify(storeNoticeRepository, never()).deleteAllById(any());
        }

        @Test
        @DisplayName("삭제할 ID 목록이 null이면 예외가 발생한다")
        void deleteNotice_NullIdList_ThrowsException() {
            // given
            StoreNoticeDeleteDto deleteDto = new StoreNoticeDeleteDto(null);

            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId))
                    .willReturn(Optional.of(mock(UserStoreRole.class)));

            // when & then
            assertThatThrownBy(() -> storeNoticeService.deleteNotice(deleteDto, storeId, userId))
                    .isInstanceOf(StoreException.class)
                    .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.INVALID_PARAMETER);

            verify(userStoreRoleRepository).findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId);
            verify(storeNoticeRepository, never()).findAllById(any());
            verify(storeNoticeRepository, never()).deleteAllById(any());
        }

        @Test
        @DisplayName("사용자가 가게 권한이 없으면 예외가 발생한다")
        void deleteNotice_UserHasNoPermission_ThrowsException() {
            // given
            List<Long> deleteIds = List.of(1L);
            StoreNoticeDeleteDto deleteDto = new StoreNoticeDeleteDto(deleteIds);

            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> storeNoticeService.deleteNotice(deleteDto, storeId, userId))
                    .isInstanceOf(StoreException.class)
                    .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.STORE_NOT_FOUND);

            verify(userStoreRoleRepository).findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId);
            verify(storeNoticeRepository, never()).findAllById(any());
            verify(storeNoticeRepository, never()).deleteAllById(any());
        }
    }
}
