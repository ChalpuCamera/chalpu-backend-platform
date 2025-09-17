package com.example.chalpuplatform.fooditem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash-latest}")
    private String modelName;

    public Mono<String> extractTextFromImage(byte[] imageBytes, String mimeType) {
        log.info("Thread [{}]: Gemini API 호출 시작 - 이미지 크기: {} bytes",
                Thread.currentThread().getName(), imageBytes.length);

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        Map<String, Object> requestBody = buildGeminiRequest(base64Image, mimeType);

        return webClient.post()
                .uri("/models/{model}:generateContent?key={key}", modelName, apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(response ->
                    log.info("Thread [{}]: Gemini API 응답 수신", Thread.currentThread().getName())
                )
                .map(this::extractTextFromResponse)
                .doOnError(error ->
                    log.error("Thread [{}]: Gemini API 호출 실패 - {}",
                            Thread.currentThread().getName(), error.getMessage(), error)
                );
    }

    private Map<String, Object> buildGeminiRequest(String base64Image, String mimeType) {
        Map<String, Object> request = new HashMap<>();

        String prompt = """
            당신은 한국 음식점 메뉴를 정확하게 읽고 분석하는 전문가입니다.
            이미지를 자세히 분석하여 모든 메뉴 항목을 찾아 추출해주세요.

            **반드시 JSON 형식으로만 응답하세요:**
            {
              "items": [
                {
                  "name": "정확한 메뉴명",
                  "price": 숫자만(콤마 제외),
                  "description": "메뉴 설명(없으면 null)",
                  "category": "카테고리"
                }
              ]
            }

            **중요 지침:**
            1. 이미지에 보이는 모든 메뉴 항목을 빠짐없이 추출하세요
            2. 메뉴명은 정확히 이미지에 나온 그대로 작성하세요
            3. 가격은 원(₩) 기호나 콤마 없이 숫자만 입력 (예: 8000)
            4. 카테고리는 다음 중 선택: 메인요리, 사이드, 음료, 디저트, 전채요리, 스프, 샐러드, 기타
            5. 세트메뉴나 콤보는 개별 항목으로 분리하지 말고 하나로 처리
            6. 읽기 어려운 부분은 추측하지 말고 건너뛰세요
            7. JSON 외의 다른 텍스트는 절대 포함하지 마세요
            """;

        Map<String, Object> imageData = new HashMap<>();
        imageData.put("mimeType", mimeType != null ? mimeType : "image/jpeg");
        imageData.put("data", base64Image);

        Map<String, Object> inlineData = new HashMap<>();
        inlineData.put("inlineData", imageData);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(inlineData, textPart));

        request.put("contents", List.of(content));

        // 생성 파라미터 설정
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.1);
        generationConfig.put("maxOutputTokens", 4096);
        generationConfig.put("topP", 0.95);
        generationConfig.put("topK", 40);
        request.put("generationConfig", generationConfig);

        return request;
    }

    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            log.debug("Thread [{}]: Gemini 응답 파싱", Thread.currentThread().getName());

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                if (content != null) {
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        Map<String, Object> part = parts.get(0);
                        Object text = part.get("text");
                        if (text != null) {
                            String extractedText = text.toString();

                            // JSON 부분만 추출 (Gemini가 추가 텍스트를 포함할 수 있음)
                            int startIndex = extractedText.indexOf("{");
                            int endIndex = extractedText.lastIndexOf("}");
                            if (startIndex != -1 && endIndex != -1) {
                                extractedText = extractedText.substring(startIndex, endIndex + 1);
                            }

                            log.info("Thread [{}]: 텍스트 추출 성공", Thread.currentThread().getName());
                            return extractedText;
                        }
                    }
                }
            }

            log.error("Thread [{}]: 예상치 못한 응답 형식", Thread.currentThread().getName());
            throw new RuntimeException("Gemini 응답 파싱 실패: 예상치 못한 형식");

        } catch (Exception e) {
            log.error("Thread [{}]: Gemini 응답 파싱 실패", Thread.currentThread().getName(), e);
            throw new RuntimeException("응답 파싱 실패", e);
        }
    }
}