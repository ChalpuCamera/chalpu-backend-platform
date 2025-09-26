package com.example.chalpuplatform.user.controller;

import com.example.chalpuplatform.common.exception.AuthException;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.user.dto.UserDto;
import com.example.chalpuplatform.user.dto.CustomerTasteDto;
import com.example.chalpuplatform.user.service.UserProfileService;
import com.example.chalpuplatform.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 정보 관련 API")
public class UserController {

    private final UserService userService;
    private final UserProfileService userProfileService;

    @Operation(
        summary = "현재 사용자 정보 조회",
        description = "JWT 인증을 통해 현재 로그인한 사용자의 상세 정보를 조회합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        
        if (currentUser == null) {
            throw new AuthException(ErrorMessage.AUTH_UNAUTHORIZED);
        }

        // 서비스를 통해 사용자 정보 조회
        User user = userService.getUserById(currentUser.getId());
        UserDto userDto = new UserDto(user);
        
        return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회가 완료되었습니다.", userDto));
    }

    @Operation(
        summary = "사용자 정보 삭제",
        description = "사용자 정보를 삭제합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        userService.softDelete(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    @Operation(
        summary = "고객 취향 정보 조회",
        description = "현재 로그인한 사용자의 고객 취향 정보(매운맛, 식사량, 식사 지출)를 조회합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/profile/taste")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerTasteDto>> getCustomerTaste(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        
        if (currentUser == null) {
            throw new AuthException(ErrorMessage.AUTH_UNAUTHORIZED);
        }
        
        CustomerTasteDto customerTaste = userProfileService.getCustomerTaste(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("고객 취향 정보 조회가 완료되었습니다.", customerTaste));
    }
    
    @Operation(
        summary = "고객 취향 정보 수정",
        description = "현재 로그인한 사용자의 고객 취향 정보(매운맛, 식사량, 식사 지출)를 수정합니다. 각 값은 1-5 범위여야 합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PutMapping("/profile/taste")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerTasteDto>> updateCustomerTaste(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody CustomerTasteDto customerTasteDto) {
        
        if (currentUser == null) {
            throw new AuthException(ErrorMessage.AUTH_UNAUTHORIZED);
        }
        
        CustomerTasteDto updatedCustomerTaste = userProfileService.updateCustomerTaste(
                currentUser.getId(), customerTasteDto);
        return ResponseEntity.ok(ApiResponse.success("고객 취향 정보가 수정되었습니다.", updatedCustomerTaste));
    }
}
