package com.example.chalpuplatform.store.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.common.response.PageResponse;
import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import com.example.chalpuplatform.store.dto.CreateStoreNoticeRequest;
import com.example.chalpuplatform.store.dto.StoreNoticeDeleteDto;
import com.example.chalpuplatform.store.dto.StoreNoticeResponse;
import com.example.chalpuplatform.store.dto.UpdateStoreNoticeRequest;
import com.example.chalpuplatform.store.service.StoreNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
@Tag(name = "Store Notice", description = "가게 공지사항 API")
public class StoreNoticeController {

    private final StoreNoticeService storeNoticeService;

    /**
     * 가게 공지사항을 생성합니다.
     *
     * @param storeId 가게 ID
     * @param request 공지사항 생성 요청 데이터
     * @return 생성된 공지사항 정보
     */
    @PostMapping("/{storeId}/notices")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "가게 공지사항 생성", description = "새로운 가게 공지사항을 생성합니다. 매장 관리 권한이 필요합니다.")
    public ResponseEntity<ApiResponse<StoreNoticeResponse>> createNotice(
            @PathVariable("storeId") Long storeId,
            @RequestBody @Valid CreateStoreNoticeRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        StoreNoticeResponse response = storeNoticeService.createNotice(storeId, request,userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 가게의 공지사항 목록을 조회합니다.
     *
     * @param storeId 가게 ID
     * @param pageable 페이징 정보
     * @return 공지사항 페이지 응답
     */
    @GetMapping("/{storeId}/notices")
    @Operation(
            summary = "가게 공지사항 목록 조회",
            description = """
                    특정 가게의 공지사항 목록을 페이지네이션으로 조회합니다.

                    **페이지네이션 파라미터:**
                    - page: 페이지 번호 (0부터 시작, 기본값: 0)
                    - size: 페이지 크기 (기본값: 20)
                    - sort: 정렬 조건 (기본값: createdAt,desc)
                    """
    )
    public ResponseEntity<ApiResponse<PageResponse<StoreNoticeResponse>>> getNotices(
            @PathVariable("storeId") Long storeId,
            @Parameter(description = "페이지네이션 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<StoreNoticeResponse> notices = storeNoticeService.getNotices(storeId, pageable);
        return ResponseEntity.ok(ApiResponse.success(notices));
    }

    /**
     * 공지사항을 수정합니다.
     *
     * @param noticeId 공지사항 ID
     * @param request 공지사항 수정 요청 데이터
     * @return 수정된 공지사항 정보
     */
    @PutMapping("/notices/{noticeId}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "가게 공지사항 수정", description = "가게 공지사항 내용을 수정합니다. 매장 관리 권한이 필요합니다.")
    public ResponseEntity<ApiResponse<StoreNoticeResponse>> updateNotice(
            @PathVariable("noticeId") Long noticeId,
            @RequestBody @Valid UpdateStoreNoticeRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        StoreNoticeResponse response = storeNoticeService.updateNotice(noticeId, request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 공지사항을 삭제합니다.
     *
     * @param storeNoticeDeleteDto 공지사항 ID
     * @return 삭제 성공 응답
     */
    @DeleteMapping("/{storeId}/notices")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "가게 공지사항 삭제", description = "가게 공지사항을 삭제합니다. 매장 관리 권한이 필요합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(
            @PathVariable(name = "storeId") Long storeId,
            @RequestBody StoreNoticeDeleteDto storeNoticeDeleteDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        storeNoticeService.deleteNotice(storeNoticeDeleteDto, storeId,userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
