package com.example.chalpuplatform.landing.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.landing.domain.ContactInquiry;
import com.example.chalpuplatform.landing.domain.LandingPageButtonLog;
import com.example.chalpuplatform.landing.dto.ButtonLogRequest;
import com.example.chalpuplatform.landing.dto.ContactInquiryRequest;
import com.example.chalpuplatform.landing.repository.ContactInquiryRepository;
import com.example.chalpuplatform.landing.repository.LandingPageButtonLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/landing")
@RequiredArgsConstructor
@Tag(name = "Landing", description = "랜딩페이지 퍼널 분석 API")
public class LandingController {

    private final LandingPageButtonLogRepository buttonLogRepository;
    private final ContactInquiryRepository inquiryRepository;

    @PostMapping("/button-log")
    @Operation(
            summary = "버튼 클릭 로그 저장",
            description = "랜딩페이지에서 버튼 클릭 시 로그를 저장합니다. " +
                    "버튼 타입: START_FREE(무료로 시작하기), KAKAO_LOGIN(카카오 로그인)"
    )
    public ResponseEntity<ApiResponse<Void>> logButtonClick(@RequestBody ButtonLogRequest request) {
        log.info("Button click logged: {}", request.getButtonType());

        LandingPageButtonLog log = LandingPageButtonLog.createLog(request.getButtonType());
        buttonLogRepository.save(log);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/inquiry")
    @Operation(
            summary = "문의 내용 저장",
            description = "랜딩페이지에서 문의 내용을 저장합니다."
    )
    public ResponseEntity<ApiResponse<Void>> saveInquiry(@RequestBody ContactInquiryRequest request) {
        log.info("Contact inquiry saved");

        ContactInquiry inquiry = ContactInquiry.createInquiry(request.getContent());
        inquiryRepository.save(inquiry);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
