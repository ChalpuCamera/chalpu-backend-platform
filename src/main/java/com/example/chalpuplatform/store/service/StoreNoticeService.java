package com.example.chalpuplatform.store.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.StoreException;
import com.example.chalpuplatform.common.response.PageResponse;
import com.example.chalpuplatform.store.domain.StoreNotice;
import com.example.chalpuplatform.store.dto.CreateStoreNoticeRequest;
import com.example.chalpuplatform.store.dto.StoreNoticeDeleteDto;
import com.example.chalpuplatform.store.dto.StoreNoticeResponse;
import com.example.chalpuplatform.store.dto.UpdateStoreNoticeRequest;
import com.example.chalpuplatform.store.repository.StoreNoticeRepository;
import com.example.chalpuplatform.store.repository.UserStoreRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreNoticeService {

    private final StoreNoticeRepository storeNoticeRepository;
    private final UserStoreRoleRepository userStoreRoleRepository;

    /**
     * 가게 공지사항을 생성합니다.
     *
     * @param storeId 가게 ID
     * @param request 공지사항 생성 요청 데이터
     * @return 생성된 공지사항 정보
     * @throws StoreException 가게를 찾을 수 없는 경우
     */
    public StoreNoticeResponse createNotice(Long storeId, CreateStoreNoticeRequest request,Long userId) {
        try {
            userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId)
                    .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));

            StoreNotice notice = StoreNotice.builder()
                    .storeId(storeId)
                    .title(request.getTitle())
                    .body(request.getBody())
                    .build();

            StoreNotice savedNotice = storeNoticeRepository.save(notice);

            log.info("event=store_notice_created, store_id={}, notice_id={}", storeId, savedNotice.getId());
            return StoreNoticeResponse.from(savedNotice);
        } catch (StoreException e) {
            throw e;
        } catch (Exception e) {
            log.error("event=store_notice_creation_failed, store_id={}, error_message={}", storeId, e.getMessage(), e);
            throw new StoreException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 가게의 공지사항 목록을 조회합니다.
     *
     * @param storeId 가게 ID
     * @param pageable 페이징 정보
     * @return 공지사항 페이지 응답
     */
    @Transactional(readOnly = true)
    public PageResponse<StoreNoticeResponse> getNotices(Long storeId, Pageable pageable) {
        try {
            Page<StoreNotice> noticePage = storeNoticeRepository.findByStoreId(storeId, pageable);
            Page<StoreNoticeResponse> responsePage = noticePage.map(StoreNoticeResponse::from);

            log.info("event=store_notices_retrieved, store_id={}, total_count={}", storeId, noticePage.getTotalElements());
            return PageResponse.from(responsePage);
        } catch (Exception e) {
            log.error("event=store_notices_retrieval_failed, store_id={}, error_message={}", storeId, e.getMessage(), e);
            throw new StoreException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 공지사항을 수정합니다.
     *
     * @param noticeId 공지사항 ID
     * @param request 공지사항 수정 요청 데이터
     * @param userId 사용자 ID
     * @return 수정된 공지사항 정보
     * @throws StoreException 공지사항을 찾을 수 없는 경우
     */
    public StoreNoticeResponse updateNotice(Long noticeId, UpdateStoreNoticeRequest request, Long userId) {
        try {
            StoreNotice notice = storeNoticeRepository.findById(noticeId)
                    .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOTICE_NOT_FOUND));

            userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(userId, notice.getStoreId())
                    .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));

            notice.update(request.getTitle(), request.getBody());

            log.info("event=store_notice_updated, notice_id={}, store_id={}", noticeId, notice.getStoreId());
            return StoreNoticeResponse.from(notice);
        } catch (StoreException e) {
            throw e;
        } catch (Exception e) {
            log.error("event=store_notice_update_failed, notice_id={}, error_message={}", noticeId, e.getMessage(), e);
            throw new StoreException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 공지사항을 벌크 삭제합니다.
     *
     * @param dto 삭제할 공지사항 ID 목록
     * @param userId 사용자 ID
     * @throws StoreException 공지사항을 찾을 수 없는 경우
     */
    public void deleteNotice(StoreNoticeDeleteDto dto, Long storeId,Long userId) {
        try {
            userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId)
                    .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));

            List<Long> noticeIds = dto.getDeleteIds();

            if (noticeIds == null || noticeIds.isEmpty()) {
                throw new StoreException(ErrorMessage.INVALID_PARAMETER);
            }

            List<StoreNotice> notices = storeNoticeRepository.findAllById(noticeIds);

            if (notices.size() != noticeIds.size()) {
                throw new StoreException(ErrorMessage.STORE_NOTICE_NOT_FOUND);
            }

            storeNoticeRepository.deleteAllById(noticeIds);

            log.info("event=store_notices_bulk_deleted, count={}, notice_ids={}", noticeIds.size(), noticeIds);
        } catch (StoreException e) {
            throw e;
        } catch (Exception e) {
            log.error("event=store_notices_bulk_deletion_failed, error_message={}", e.getMessage(), e);
            throw new StoreException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
