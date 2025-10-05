package com.example.chalpuplatform.test;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.fooditem.service.GeminiService;
import com.example.chalpuplatform.oauth.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Profile({"local"}) // dev, prod 환경에서는 이 컨트롤러가 활성화되지 않음
@Tag(name = "Test", description = "테스트 API")
public class TestTokenController {

    private final JwtTokenProvider jwtTokenProvider;
    private final GeminiService geminiService;

    @GetMapping("/token")
    public ApiResponse<String> generateTestToken(
            @RequestParam(defaultValue = "1") Long userId,
            @RequestParam(defaultValue = "test@example.com") String email,
            @RequestParam(defaultValue = "ROLE_CUSTOMER") String role) {

        String token = jwtTokenProvider.generateTestAccessToken(userId, email, role);

        log.info("테스트 토큰 생성: userId={}, email={}, role={}", userId, email, role);
        log.info("생성된 토큰: {}", token);

        return ApiResponse.success(token);
    }

    @PostMapping(value = "/gemini-blocking", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Gemini 블로킹 테스트", description = "블로킹 방식으로 Gemini API를 호출하여 스레드 대기 시간을 측정합니다")
    public ApiResponse<Map<String, Object>> testGeminiBlocking(
            @RequestPart("image") @Parameter(description = "분석할 이미지 파일", required = true) MultipartFile image) throws IOException {

        byte[] imageBytes = image.getBytes();
        String mimeType = image.getContentType();
        String threadName = Thread.currentThread().getName();

        log.info("[{}] 블로킹 방식 Gemini API 호출 시작", threadName);

        Instant startTime = Instant.now();

        // 블로킹 방식: 스레드가 I/O 완료까지 대기
        String result = geminiService.extractTextFromImage(imageBytes, mimeType)
                .block(Duration.ofSeconds(30));

        Instant endTime = Instant.now();
        long threadWaitTime = Duration.between(startTime, endTime).toMillis();

        log.info("[{}] 블로킹 방식 완료 - 스레드 대기 시간: {}ms", threadName, threadWaitTime);

        Map<String, Object> response = new HashMap<>();
        response.put("method", "BLOCKING");
        response.put("threadName", threadName);
        response.put("threadWaitTime", threadWaitTime + "ms");
        response.put("description", "스레드가 I/O 작업 완료까지 블로킹됨");
        response.put("resultLength", result != null ? result.length() : 0);

        return ApiResponse.success(response);
    }

    @PostMapping(value = "/gemini-nonblocking", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Gemini 논블로킹 테스트", description = "논블로킹 방식으로 Gemini API를 호출하여 스레드 대기 시간을 측정합니다")
    public Mono<ApiResponse<Map<String, Object>>> testGeminiNonBlocking(
            @RequestPart("image") @Parameter(description = "분석할 이미지 파일", required = true) MultipartFile image) throws IOException {

        byte[] imageBytes = image.getBytes();
        String mimeType = image.getContentType();
        String threadName = Thread.currentThread().getName();

        log.info("[{}] 논블로킹 방식 Gemini API 호출 시작", threadName);

        Instant startTime = Instant.now();

        // 논블로킹 방식: 스레드가 즉시 반환되고 나중에 콜백으로 처리
        return geminiService.extractTextFromImage(imageBytes, mimeType)
                .map(result -> {
                    Instant endTime = Instant.now();
                    long totalTime = Duration.between(startTime, endTime).toMillis();
                    String callbackThreadName = Thread.currentThread().getName();

                    // 스레드 대기 시간은 거의 0 (즉시 반환)
                    long threadWaitTime = 1; // 논블로킹이므로 실제 대기 시간 거의 없음

                    log.info("[{}] 논블로킹 콜백 처리 완료 - 전체 시간: {}ms",
                            callbackThreadName, totalTime);

                    Map<String, Object> response = new HashMap<>();
                    response.put("method", "NON_BLOCKING");
                    response.put("requestThreadName", threadName);
                    response.put("callbackThreadName", callbackThreadName);
                    response.put("threadWaitTime", threadWaitTime + "ms");
                    response.put("totalAsyncTime", totalTime + "ms");
                    response.put("description", "스레드가 즉시 반환되어 다른 작업 처리 가능");
                    response.put("resultLength", result != null ? result.length() : 0);

                    return ApiResponse.success(response);
                });
    }
}