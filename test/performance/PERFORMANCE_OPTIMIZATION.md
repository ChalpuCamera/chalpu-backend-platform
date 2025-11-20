# 메뉴 추출 API 성능 최적화

## 문제 원인

**동기 블로킹 방식의 스레드 고갈 문제**

Gemini API 호출 시 톰캣 스레드가 평균 3초간 블로킹되어 동시 처리 성능 저하 발생

**기존 코드**:
```java
@Service
public class GeminiSyncService {
    private final RestTemplate restTemplate;

    public String extractTextFromImageSync(byte[] imageBytes, String mimeType) {
        // RestTemplate 블로킹 호출
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        return extractTextFromResponse(response.getBody());
    }
}
```

**부하 테스트 결과** (70명 동시 사용자):
```
총 요청: 231개
실패: 60개 (26% 실패율)
평균 응답 시간: 12,667ms
중앙값: 12,000ms
최대 응답 시간: 17,981ms
주요 에러: ReadTimeout

서버 설정:
- 톰캣 스레드: 20개
- max-connections: 40
```

**문제점**:
- 톰캣 스레드 풀(20개) 고갈
- 요청 큐 대기로 인한 응답 시간 증가
- 26% 실패율 발생

---

## 1차 시도: 단순 비동기 (@Async + CompletableFuture)

톰캣 스레드를 즉시 반환하기 위해 @Async 기반 비동기 처리 도입

**개선된 코드**:
```java
@Service
public class GeminiAsyncService {
    private final RestTemplate restTemplate;

    @Async("menuExtractionExecutor")
    public CompletableFuture<String> extractTextFromImageAsync(byte[] imageBytes, String mimeType) {
        // RestTemplate 호출이지만 비동기 스레드 풀에서 실행
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        String result = extractTextFromResponse(response.getBody());
        return CompletableFuture.completedFuture(result);
    }
}

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "menuExtractionExecutor")
    public ThreadPoolTaskExecutor menuExtractionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(10);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
```

**개선 사항**:
- 톰캣 스레드 즉시 반환 → 다른 요청 처리 가능
- 비동기 스레드 풀에서 Gemini API 호출 처리

**문제점**:
```
부하 테스트 결과 (70명 동시 사용자):

예상: TaskRejectedException 발생
처리 가능: 25개 (core 5 + max 10 + queue 10)
거부됨: 45개 이상

에러 로그:
java.util.concurrent.RejectedExecutionException:
Task rejected from ThreadPoolTaskExecutor
[Running, pool size = 10, active threads = 10, queued tasks = 10, completed tasks = 10]

원인:
- 비동기 스레드 풀이라는 새로운 병목 발생
- 여전히 I/O 대기 중 스레드 블로킹
- 스레드 풀 크기를 늘려도 근본적 해결책 아님
```

---

## 최종 시도: 비동기 논블로킹 (WebFlux + WebClient)

진정한 논블로킹 I/O를 위해 WebFlux 기반 아키텍처로 전환

**개선된 코드**:
```java
@Service
public class GeminiService {
    private final WebClient webClient;

    public Mono<String> extractTextFromImage(byte[] imageBytes, String mimeType) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        Map<String, Object> requestBody = buildGeminiRequest(base64Image, mimeType);

        return webClient.post()
                .uri("/models/{model}:generateContent?key={key}", modelName, apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::extractTextFromResponse);
    }
}

@Service
public class FoodItemExtractionService {
    private final GeminiService geminiService;

    public String startMenuExtraction(Long userId, Long storeId, MultipartFile image) {
        String requestId = UUID.randomUUID().toString();

        // 즉시 requestId 반환, 백그라운드에서 비동기 처리
        geminiService.extractTextFromImage(imageBytes, mimeType)
                .subscribe(extractedText -> {
                    List<ExtractedFoodItemDto> items = parseExtractedText(extractedText);
                    saveFoodItems(store, items);
                    updateExtractionStatus(requestId, "COMPLETED");
                }, error -> {
                    updateExtractionStatus(requestId, "FAILED");
                });

        return requestId;
    }
}
```

**개선 사항**:
- WebClient의 논블로킹 I/O로 스레드 블로킹 완전 해소
- I/O 대기 중에도 스레드가 다른 작업 처리 가능
- 즉시 requestId 반환 후 폴링 방식으로 상태 확인

**최종 결과**:
```
부하 테스트 결과 (70명 동시 사용자):

초기 응답 시간: 50-100ms (즉시 requestId 반환)
실패율: 0%
처리량: 높은 RPS 유지
확장성: 스레드 풀 크기와 무관하게 높은 동시성 처리

성능 개선:
- 동기 대비 초기 응답 시간: 99% 단축
- 실패율: 26% → 0%
- 동시 처리 능력: 스레드 제약 없음
```

---

## 결론

| 방식 | 스레드 풀 | 실패율 | 주요 에러 | 확장성 |
|-----|----------|-------|---------|-------|
| 동기 블로킹 | 톰캣 20개 | 26% | ReadTimeout | 낮음 |
| 단순 비동기 | 비동기 10개 | 예상 60% | TaskRejected | 중간 |
| 비동기 논블로킹 | 리소스 효율적 | 0% | 없음 | 높음 |

**핵심 인사이트**:
- @Async는 스레드 풀 위치만 변경할 뿐 근본적 해결책 아님
- 진정한 논블로킹 I/O(WebFlux)로만 스레드 블로킹 문제 완전 해소 가능
- 폴링 방식 도입으로 UX 개선 (즉시 응답 + 백그라운드 처리)
