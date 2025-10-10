package com.example.chalpuplatform.store.controller;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.StoreException;
import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.dto.StoreIdResponse;
import com.example.chalpuplatform.store.repository.StoreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/stores")
@RequiredArgsConstructor
@Tag(name = "Store Public", description = "매장 공개 API")
public class StorePublicController {

    private final StoreRepository storeRepository;

    @GetMapping("/{siteLink}")
    @Operation(
            summary = "사이트 링크로 매장 조회",
            description = "사이트 링크로 매장 ID를 조회합니다. 인증이 필요없는 공개 API입니다."
    )
    public ResponseEntity<ApiResponse<StoreIdResponse>> getStoreBySiteLink(
            @PathVariable("siteLink") @Parameter(description = "사이트 링크") String siteLink) {

        Store store = storeRepository.findBySiteLink(siteLink)
                .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success(StoreIdResponse.from(store)));
    }
}
