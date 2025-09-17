package com.example.chalpuplatform.fooditem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Deprecated // Use GeminiService instead - Gemini 2.0 Flash is free
public class BedrockService {

    private final BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Claude 3.5 Sonnet이 더 정확함
    private static final String CLAUDE_MODEL_ID = "anthropic.claude-3-5-sonnet-20240620-v1:0";
    private static final int MAX_TOKENS = 4096;
    private static final double TEMPERATURE = 0.1;  // 더 낮은 temperature로 정확도 향상

    public String extractTextFromImage(byte[] imageBytes, String mimeType) {
        try {
            String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
            String requestBody = buildClaudeRequest(base64Image, mimeType);

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(CLAUDE_MODEL_ID)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(requestBody))
                    .build();

            log.info("베드록 AI 모델 호출 시작 - 이미지 크기: {} bytes", imageBytes.length);

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            return extractResponseText(response);

        } catch (Exception e) {
            log.error("베드록 API 호출 실패 - 에러: {}", e.getMessage(), e);
            throw new RuntimeException("메뉴 텍스트 추출 실패", e);
        }
    }

    private String buildClaudeRequest(String base64Image, String mimeType) throws Exception {
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

        request.put("anthropic_version", "bedrock-2023-05-31");
        request.put("max_tokens", MAX_TOKENS);
        request.put("temperature", TEMPERATURE);
        request.put("messages", new Object[]{
                Map.of(
                        "role", "user",
                        "content", new Object[]{
                                Map.of(
                                        "type", "image",
                                        "source", Map.of(
                                                "type", "base64",
                                                "media_type", mimeType != null ? mimeType : "image/jpeg",
                                                "data", base64Image
                                        )
                                ),
                                Map.of(
                                        "type", "text",
                                        "text", prompt
                                )
                        }
                )
        });

        return objectMapper.writeValueAsString(request);
    }

    private String extractResponseText(InvokeModelResponse response) {
        try {
            String responseBody = response.body().asUtf8String();
            log.debug("베드록 응답: {}", responseBody);

            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            Object content = responseMap.get("content");

            if (content instanceof java.util.List<?> contentList) {
                if (!contentList.isEmpty() && contentList.get(0) instanceof Map<?, ?> firstContent) {
                    Object text = firstContent.get("text");
                    if (text != null) {
                        return text.toString();
                    }
                }
            }

            log.warn("예상치 못한 응답 형식: {}", responseBody);
            return responseBody;

        } catch (Exception e) {
            log.error("베드록 응답 파싱 실패", e);
            throw new RuntimeException("응답 파싱 실패", e);
        }
    }
}