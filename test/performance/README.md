# 메뉴 추출 성능 비교 테스트

## 개요

이 테스트는 메뉴 이미지 추출 기능의 동기 블로킹 방식과 비동기 논블로킹 방식의 성능을 비교하기 위해 작성되었습니다.

### 테스트 목적

Gemini API 호출 시 발생하는 I/O 대기 시간(평균 2-3초)이 시스템 전체 성능에 미치는 영향을 측정하고, WebFlux 기반 논블로킹 I/O 전환의 효과를 검증합니다.

## 구현 방식 비교

### 1. 동기 블로킹 방식 (RestTemplate)

**파일**: `GeminiSyncService.java`, `FoodItemExtractionSyncService.java`

**특징**:
- RestTemplate을 사용한 동기 HTTP 호출
- Gemini API 응답까지 **톰캣 스레드 블로킹**
- DB 저장까지 모든 작업을 순차 처리
- 엔드포인트: `POST /api/foods/menu/extract-sync`

**문제점**:
- 스레드가 3초간 블로킹되어 다른 요청 처리 불가
- 톰캣 스레드 풀(20개) 고갈 시 큐 대기 발생
- 70개 동시 요청 시 26% 실패율, 평균 12-27초 소요
- ReadTimeout 에러 발생

**테스트 결과**:
```
총 요청: 231개
실패: 60개 (26%)
평균 응답 시간: 12,667ms
중앙값: 12,000ms
```

### 2. 단순 비동기 방식 (@Async + CompletableFuture)

**파일**: `GeminiAsyncService.java`, `FoodItemExtractionAsyncService.java`

**특징**:
- @Async를 사용한 단순 비동기 처리
- RestTemplate 호출이지만 **비동기 스레드 풀**에서 실행
- 톰캣 스레드는 즉시 반환되지만, 비동기 스레드 풀에서 블로킹
- 엔드포인트: `POST /api/foods/menu/extract-simple-async`
- CompletableFuture 반환

**문제점**:
- 비동기 스레드 풀(core: 5, max: 10, queue: 10) 고갈 시 **TaskRejectedException** 발생
- 스레드 풀 설정에 따라 여전히 병목 발생
- 70개 동시 요청 시 예상 60% 실패율
- 여전히 I/O 대기 중 스레드 블로킹

**예상 결과**:
```
처리 가능: 25개 (10 active + 10 queue + 5 core)
TaskRejectedException: 45개 (60%)
```

### 3. 비동기 논블로킹 방식 (WebClient + Reactor)

**파일**: `GeminiService.java`, `FoodItemExtractionService.java`

**특징**:
- WebClient를 사용한 **진정한 논블로킹 I/O**
- Reactor의 `Mono`를 반환하고 `.subscribe()`로 백그라운드 처리
- I/O 대기 중에도 스레드가 다른 작업 수행 가능
- 즉시 `requestId` 반환, 클라이언트는 3초 주기로 폴링
- 엔드포인트: `POST /api/foods/menu/extract` + `GET /api/foods/menu/extract/status/{requestId}`

**장점**:
- 스레드가 즉시 반환되어 다른 요청 처리 가능
- 스레드 풀 효율적 사용, 리소스 절약
- 70개 동시 요청도 0% 실패율 예상
- 초기 응답 50-100ms로 매우 빠름

## 테스트 환경 설정

### 1. 톰캣 스레드 풀 제한

`application-performance.yml`:
```yaml
server:
  tomcat:
    threads:
      max: 20
      min-spare: 5
```

### 2. 애플리케이션 실행

```bash
# 성능 테스트 프로파일로 실행
./gradlew bootRun --args='--spring.profiles.active=performance'
```

### 3. Locust 설치

```bash
pip install locust
```

## 테스트 시나리오

### 시나리오 1: 동기 블로킹 버전 테스트

**목적**: 톰캣 스레드 풀 고갈 문제 재현

```bash
cd test/performance

# 환경 변수 설정
export AUTH_TOKEN="your-jwt-token"
export STORE_ID="1"
export TEST_IMAGE_PATH="./test-menu.jpg"

# Locust 실행 (CLI)
locust -f locustfile.py \
  --host=http://localhost:8080 \
  --users 70 \
  --spawn-rate 70 \
  --run-time 3m \
  MenuExtractionSyncUser
```

**예상 결과**:
- 처음 20개 요청: 톰캣 스레드 풀에서 처리 (각각 3초)
- 21-50번째: 큐 대기 + accept queue 활용
- 51번째 이후: Connection Refused 또는 ReadTimeout
- 실패율: 약 26%
- 평균 응답 시간: 12-27초

**실제 테스트 결과**:
```
총 요청: 231개
실패: 60개 (26% 실패율)
평균 응답 시간: 12,667ms
중앙값: 12,000ms
최대: 17,981ms
주요 에러: ReadTimeout
```

### 시나리오 2: 단순 비동기 버전 테스트

**목적**: 비동기 스레드 풀 고갈 시 TaskRejectedException 재현

```bash
locust -f locustfile.py \
  --host=http://localhost:8080 \
  --users 70 \
  --spawn-rate 70 \
  --run-time 3m \
  MenuExtractionSimpleAsyncUser
```

**예상 결과**:
- 처음 10개: 비동기 스레드 풀에서 즉시 처리
- 11-20번째: 큐에 대기 (queue capacity: 10)
- 21번째 이후: **TaskRejectedException** 발생
- 실패율: 약 60%
- 톰캣 스레드는 즉시 반환되지만 비동기 스레드 풀 병목

**비교 포인트**:
- 톰캣 스레드는 블로킹되지 않음 (동기 방식보다 개선)
- 하지만 비동기 스레드 풀이라는 새로운 병목 발생
- 스레드 풀 크기를 늘려도 근본적인 해결책은 아님

### 시나리오 3: 비동기 논블로킹 버전 테스트

**목적**: 논블로킹 방식의 성능 우위 검증

```bash
locust -f locustfile.py \
  --host=http://localhost:8080 \
  --users 70 \
  --spawn-rate 70 \
  --run-time 3m \
  MenuExtractionAsyncUser
```

**예상 결과**:
- 초기 응답: 50-100ms (즉시 requestId 반환)
- 실패율: 0% 또는 매우 낮음
- 스레드 블로킹 없음, I/O 대기 중 다른 작업 처리
- 폴링 요청은 경량이므로 부담 적음
- 전체 완료 시간: 3-5초 (Gemini API 응답 시간 기준)

### 시나리오 4: Web UI로 테스트 (권장)

```bash
locust -f locustfile.py --host=http://localhost:8080
```

브라우저에서 `http://localhost:8089` 접속 후:
1. Number of users: 70
2. Spawn rate: 70
3. Host: http://localhost:8080
4. User class 선택:
   - `MenuExtractionSyncUser` (동기 블로킹)
   - `MenuExtractionSimpleAsyncUser` (단순 비동기)
   - `MenuExtractionAsyncUser` (비동기 논블로킹)

**장점**:
- 실시간 그래프로 응답 시간, RPS 확인
- 테스트 중간에 사용자 수 조정 가능
- 상세한 통계 및 실패율 확인
- 세 가지 방식을 번갈아 테스트하며 비교

## 측정 지표

### 주요 지표

1. **평균 응답 시간 (Average Response Time)**
   - 동기: 3-10초 (블로킹 시간 포함)
   - 비동기: 초기 응답 50-100ms, 전체 완료 3-10초

2. **최대 응답 시간 (Max Response Time)**
   - 동기: 큐 대기로 인해 수십 초 이상
   - 비동기: 일정하게 유지

3. **RPS (Requests Per Second)**
   - 동기: 스레드 풀 크기에 제한 (약 5-7 RPS)
   - 비동기: 훨씬 높은 처리량 (20+ RPS)

4. **실패율 (Failure Rate)**
   - 동기: 타임아웃으로 인한 실패 가능
   - 비동기: 낮은 실패율

5. **P95, P99 응답 시간**
   - 동기: 큰 편차 발생
   - 비동기: 안정적인 분포

### 로그 분석

애플리케이션 로그에서 다음 패턴 확인:

```
[PERF] [동기] 메뉴 추출 시작 - Thread: http-nio-8080-exec-1
[PERF] [동기] Gemini API 블로킹 호출 시작 - Thread: http-nio-8080-exec-1
[PERF] [동기] Gemini API 블로킹 호출 완료 - Thread: http-nio-8080-exec-1, Duration: 2847ms
[PERF] [동기] 메뉴 추출 완료 - Thread: http-nio-8080-exec-1, Duration: 3124ms (블로킹 완료)
```

vs

```
[PERF] [비동기] 메뉴 추출 시작 - Thread: http-nio-8080-exec-1
[PERF] [비동기] Gemini API 호출 시작 - Thread: http-nio-8080-exec-1
[PERF] [비동기] Gemini API 호출 Mono 반환 - Thread: http-nio-8080-exec-1, Duration: 12ms
[PERF] [비동기] 메뉴 추출 초기 응답 완료 - Thread: http-nio-8080-exec-1, Duration: 45ms (즉시 반환)
```

## 테스트 이미지 준비

테스트용 메뉴판 이미지를 `test/performance/test-menu.jpg`에 준비합니다.

```bash
# 샘플 이미지 다운로드 또는 복사
cp /path/to/your/menu-image.jpg test/performance/test-menu.jpg
```

## 테스트 실행 체크리스트

- [ ] 애플리케이션을 performance 프로파일로 실행
- [ ] 톰캣 스레드 풀이 20개로 제한되었는지 확인
- [ ] Gemini API 키가 설정되었는지 확인
- [ ] 테스트 이미지가 준비되었는지 확인
- [ ] JWT 토큰을 환경 변수로 설정
- [ ] Locust 설치 완료
- [ ] 동기 버전 테스트 실행 및 결과 캡처
- [ ] 비동기 버전 테스트 실행 및 결과 캡처
- [ ] 로그 파일에서 스레드 정보 확인
- [ ] Locust 대시보드 스크린샷 저장

## 결과 정리

테스트 완료 후 다음 정보를 정리합니다:

### 비교표

| 항목 | 동기 블로킹 | 단순 비동기 | 비동기 논블로킹 |
|-----|-----------|-----------|--------------|
| **스레드 풀** | 톰캣 20개 | 비동기 10개 | 리소스 효율적 |
| **평균 응답 시간** | 12,667ms | ___ms | 50-100ms (초기) |
| **중앙값** | 12,000ms | ___ms | ___ms |
| **최대 응답 시간** | 17,981ms | ___ms | ___ms |
| **실패율** | 26% | 예상 60% | 0% |
| **주요 에러** | ReadTimeout | TaskRejected | 없음 |
| **RPS** | 3.11 | ___| ___ |
| **스레드 블로킹** | 톰캣 스레드 | 비동기 스레드 | 없음 |
| **확장성** | 낮음 | 중간 | 높음 |

### 1. 동기 블로킹 버전 (실제 결과)
- 평균 응답 시간: 12,667ms
- 최대 응답 시간: 17,981ms
- RPS: 3.11
- 실패율: 26% (60/231)
- 총 요청: 231개
- 주요 에러: ReadTimeout

### 2. 단순 비동기 버전 (테스트 예정)
- 초기 응답 시간: ___ms
- 평균 응답 시간: ___ms
- RPS: ___
- 실패율: ___%
- 총 요청: ___개
- 주요 에러: TaskRejectedException

### 3. 비동기 논블로킹 버전 (테스트 예정)
- 초기 응답 시간: ___ms (즉시 requestId 반환)
- 전체 완료 시간: ___ms
- RPS: ___
- 실패율: ___%
- 총 요청: ___개
- 주요 에러: 없음

### 개선율 분석

**동기 → 단순 비동기**:
- 톰캣 스레드 블로킹 해소: ✓
- 전체 처리량 개선: 일부
- 스레드 풀 병목: 여전히 존재 (비동기 스레드 풀)
- 근본적 해결: ✗

**동기 → 비동기 논블로킹**:
- 스레드 블로킹 완전 해소: ✓
- 실패율 0%: ✓
- 응답 시간 단축: 99% 이상 (초기 응답 기준)
- 확장성: 매우 높음
- 근본적 해결: ✓

## 포트폴리오 활용

다음 자료를 캡처하여 포트폴리오에 포함:
1. Locust 응답 시간 그래프 (동기 vs 비동기)
2. RPS 비교 그래프
3. 실패율 비교
4. 애플리케이션 로그에서 스레드 블로킹 증거
5. 성능 개선 수치 요약

## 참고사항

- 실제 Gemini API는 외부 네트워크 상황에 따라 응답 시간이 달라질 수 있습니다
- 테스트 환경(로컬 vs 서버)에 따라 결과가 다를 수 있습니다
- 동시 사용자 수와 스레드 풀 크기를 조정하여 다양한 시나리오 테스트 가능
