package com.example.chalpuplatform.store.controller;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.StoreException;
import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.common.response.PageResponse;
import com.example.chalpuplatform.oauth.security.jwt.UserDetailsImpl;
import com.example.chalpuplatform.store.dto.MemberInviteRequest;
import com.example.chalpuplatform.store.dto.MemberResponse;
import com.example.chalpuplatform.store.dto.StoreRequest;
import com.example.chalpuplatform.store.dto.StoreResponse;
import com.example.chalpuplatform.store.service.StoreService;
import com.example.chalpuplatform.store.service.UserStoreRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Tag(name = "Store", description = "매장 관리 API")
public class StoreController {

    private final StoreService storeService;
    private final UserStoreRoleService userStoreRoleService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "내 매장 목록 조회",
            description = """
                    사용자가 속한 매장 목록을 페이지네이션으로 조회합니다.
                    
                    **페이지네이션 파라미터:**
                    - page: 페이지 번호 (0부터 시작, 기본값: 0)
                    - size: 페이지 크기 (기본값: 10)
                    - sort: 정렬 조건 (기본값: createdAt,desc)
                    
                    **요청 예시:**
                    ```
                    GET /api/stores/my?page=0&size=10&sort=createdAt,desc
                    GET /api/stores/my?page=1&size=5&sort=name,asc
                    ```
                    
                    위처럼 정렬 조건을 문자열로 줘도 되고 아래처럼 배열로 줘도 됩니다:
                    ```
                    GET /api/stores/my?page=0&size=10&sort=createdAt&sort=desc
                    ```
                    """
    )
    public ResponseEntity<ApiResponse<PageResponse<StoreResponse>>> getMyStores(
            @Parameter(description = "페이지네이션 정보")
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        PageResponse<StoreResponse> stores = userStoreRoleService.getMyStores(userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(stores));
    }

    @GetMapping("/{storeId}")
    @Operation(summary = "매장 상세 조회", description = "특정 매장의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<StoreResponse>> getStore(
            @PathVariable Long storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        // 권한 검증: 사용자가 해당 매장에 접근할 수 있는지 확인
        if (!userStoreRoleService.canUserAccessStore(userDetails.getId(), storeId)) {
            throw new StoreException(ErrorMessage.STORE_ACCESS_DENIED);
        }
        
        StoreResponse store = storeService.getStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(store));
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "매장 생성", description = "새로운 매장을 생성합니다. 생성자는 자동으로 매장의 소유자가 됩니다.")
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(
            @RequestBody StoreRequest storeRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // 1. 매장 생성
        StoreResponse store = storeService.createStore(storeRequest);
        
        // 2. 소유자 역할 생성
        userStoreRoleService.createOwnerRole(userDetails.getId(), store.getStoreId());
        
        return ResponseEntity.ok(ApiResponse.success(store));
    }

    @PutMapping("/{storeId}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "매장 정보 수정", description = "매장 정보를 수정합니다. 매장 관리 권한이 필요합니다.")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
            @PathVariable Long storeId,
            @RequestBody StoreRequest storeRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // 권한 검증: 사용자가 이 매장을 관리할 수 있는지 확인
        if (!userStoreRoleService.canUserManageStore(userDetails.getId(), storeId)) {
            throw new StoreException(ErrorMessage.STORE_ACCESS_DENIED);
        }
        
        StoreResponse store = storeService.updateStore(storeId, storeRequest);
        return ResponseEntity.ok(ApiResponse.success(store));
    }

    @DeleteMapping("/{storeId}")
    @Operation(summary = "매장 삭제", description = "매장을 삭제합니다. 소유자만 삭제할 수 있습니다.")
    public ResponseEntity<ApiResponse<Void>> deleteStore(
            @PathVariable Long storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // 권한 검증: 소유자만 매장을 삭제할 수 있음
        if (!userStoreRoleService.canUserManageStore(userDetails.getId(), storeId)) {
            throw new StoreException(ErrorMessage.STORE_ACCESS_DENIED);
        }
        
        storeService.deleteStore(storeId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/{storeId}/members")
    @Operation(summary = "매장 멤버 목록 조회", description = "매장에 속한 멤버 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getStoreMembers(
            @PathVariable Long storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<MemberResponse> members = userStoreRoleService.getStoreMembers(storeId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @PostMapping("/{storeId}/members")
    @Operation(summary = "매장 멤버 초대", description = "매장에 새로운 멤버를 초대합니다.")
    public ResponseEntity<ApiResponse<MemberResponse>> inviteMember(
            @PathVariable Long storeId,
            @RequestBody MemberInviteRequest memberRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        MemberResponse member = userStoreRoleService.inviteMember(storeId, memberRequest, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(member));
    }
} 