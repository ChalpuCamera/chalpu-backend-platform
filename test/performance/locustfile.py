import os
import time
from locust import HttpUser, task, between
from locust import events

# 환경 변수에서 인증 토큰 읽기
AUTH_TOKEN = os.getenv("AUTH_TOKEN", "your-auth-token-here")
STORE_ID = os.getenv("STORE_ID", "1")
TEST_IMAGE_PATH = os.getenv("TEST_IMAGE_PATH", "./test-menu.jpg")


class MenuExtractionSyncUser(HttpUser):
    """
    동기 블로킹 방식 메뉴 추출 테스트
    RestTemplate 사용, 스레드가 응답까지 블로킹됨
    """
    wait_time = between(1, 2)
    host = "http://localhost:8080"

    def on_start(self):
        """사용자 시작 시 한 번 실행"""
        print(f"[SYNC] User started - {self.host}")

    @task
    def extract_menu_sync(self):
        """동기 방식으로 메뉴 추출"""
        start_time = time.time()

        headers = {
            "Authorization": f"Bearer {AUTH_TOKEN}"
        }

        try:
            with open(TEST_IMAGE_PATH, "rb") as image_file:
                files = {
                    "image": ("menu.jpg", image_file, "image/jpeg")
                }
                data = {
                    "storeId": STORE_ID
                }

                print(f"[SYNC] Sending request - Thread: {self.user_id}")

                response = self.client.post(
                    "/api/foods/menu/extract-sync",
                    files=files,
                    data=data,
                    headers=headers,
                    name="[동기] 메뉴 추출",
                    timeout=120  # 2분 타임아웃
                )

                duration = (time.time() - start_time) * 1000

                if response.status_code == 200:
                    result = response.json()
                    items_count = len(result.get("data", []))
                    print(f"[SYNC] Success - Duration: {duration:.0f}ms, Items: {items_count}")
                else:
                    print(f"[SYNC] Failed - Status: {response.status_code}, Duration: {duration:.0f}ms")

        except Exception as e:
            duration = (time.time() - start_time) * 1000
            print(f"[SYNC] Error - {str(e)}, Duration: {duration:.0f}ms")


class MenuExtractionSimpleAsyncUser(HttpUser):
    """
    단순 비동기 방식 메뉴 추출 테스트
    @Async + CompletableFuture 사용, 톰캣 스레드는 즉시 반환되지만 비동기 스레드 풀에서 블로킹됨
    """
    wait_time = between(1, 2)
    host = "http://localhost:8080"

    def on_start(self):
        """사용자 시작 시 한 번 실행"""
        print(f"[SIMPLE-ASYNC] User started - {self.host}")

    @task
    def extract_menu_simple_async(self):
        """단순 비동기 방식으로 메뉴 추출"""
        start_time = time.time()

        headers = {
            "Authorization": f"Bearer {AUTH_TOKEN}"
        }

        try:
            with open(TEST_IMAGE_PATH, "rb") as image_file:
                files = {
                    "image": ("menu.jpg", image_file, "image/jpeg")
                }
                data = {
                    "storeId": STORE_ID
                }

                print(f"[SIMPLE-ASYNC] Sending request - Thread: {self.user_id}")

                response = self.client.post(
                    "/api/foods/menu/extract-simple-async",
                    files=files,
                    data=data,
                    headers=headers,
                    name="[단순비동기] 메뉴 추출",
                    timeout=120
                )

                duration = (time.time() - start_time) * 1000

                if response.status_code == 200:
                    result = response.json()
                    items_count = len(result.get("data", []))
                    print(f"[SIMPLE-ASYNC] Success - Duration: {duration:.0f}ms, Items: {items_count}")
                else:
                    print(f"[SIMPLE-ASYNC] Failed - Status: {response.status_code}, Duration: {duration:.0f}ms")

        except Exception as e:
            duration = (time.time() - start_time) * 1000
            print(f"[SIMPLE-ASYNC] Error - {str(e)}, Duration: {duration:.0f}ms")


class MenuExtractionAsyncUser(HttpUser):
    """
    비동기 논블로킹 방식 메뉴 추출 테스트
    WebClient 사용, 즉시 requestId 반환 후 폴링
    """
    wait_time = between(1, 2)
    host = "http://localhost:8080"

    def on_start(self):
        """사용자 시작 시 한 번 실행"""
        print(f"[ASYNC] User started - {self.host}")

    @task
    def extract_menu_async(self):
        """비동기 방식으로 메뉴 추출 + 폴링"""
        start_time = time.time()

        headers = {
            "Authorization": f"Bearer {AUTH_TOKEN}"
        }

        try:
            # 1단계: 메뉴 추출 시작
            with open(TEST_IMAGE_PATH, "rb") as image_file:
                files = {
                    "image": ("menu.jpg", image_file, "image/jpeg")
                }
                data = {
                    "storeId": STORE_ID
                }

                print(f"[ASYNC] Sending initial request - Thread: {self.user_id}")

                response = self.client.post(
                    "/api/foods/menu/extract",
                    files=files,
                    data=data,
                    headers=headers,
                    name="[비동기] 메뉴 추출 시작"
                )

                initial_duration = (time.time() - start_time) * 1000

                if response.status_code != 200:
                    print(f"[ASYNC] Initial request failed - Status: {response.status_code}")
                    return

                result = response.json()
                request_id = result.get("data", {}).get("requestId")

                if not request_id:
                    print("[ASYNC] No requestId received")
                    return

                print(f"[ASYNC] Got requestId: {request_id}, Initial response: {initial_duration:.0f}ms")

                # 2단계: 상태 폴링
                max_polls = 40  # 최대 2분 (3초 * 40)
                poll_count = 0

                while poll_count < max_polls:
                    time.sleep(3)  # 3초 대기
                    poll_count += 1

                    status_response = self.client.get(
                        f"/api/foods/menu/extract/status/{request_id}",
                        headers=headers,
                        name="[비동기] 상태 조회"
                    )

                    if status_response.status_code != 200:
                        print(f"[ASYNC] Status check failed - Status: {status_response.status_code}")
                        break

                    status_result = status_response.json()
                    status_data = status_result.get("data", {})
                    status = status_data.get("status")

                    if status == "COMPLETED":
                        total_duration = (time.time() - start_time) * 1000
                        print(f"[ASYNC] Completed - Initial: {initial_duration:.0f}ms, Total: {total_duration:.0f}ms, Polls: {poll_count}")
                        break
                    elif status == "FAILED":
                        total_duration = (time.time() - start_time) * 1000
                        error_msg = status_data.get("errorMessage", "Unknown error")
                        print(f"[ASYNC] Failed - Error: {error_msg}, Duration: {total_duration:.0f}ms")
                        break
                    else:
                        progress = status_data.get("progressPercentage", 0)
                        print(f"[ASYNC] Polling ({poll_count}/{max_polls}) - Status: {status}, Progress: {progress}%")

                if poll_count >= max_polls:
                    print(f"[ASYNC] Timeout - Max polls reached")

        except Exception as e:
            duration = (time.time() - start_time) * 1000
            print(f"[ASYNC] Error - {str(e)}, Duration: {duration:.0f}ms")


@events.init_command_line_parser.add_listener
def on_locust_init(parser, **kwargs):
    """커맨드라인 인자 추가"""
    parser.add_argument("--auth-token", type=str, default="", help="인증 토큰")
    parser.add_argument("--store-id", type=str, default="1", help="매장 ID")
    parser.add_argument("--test-image", type=str, default="./test-menu.jpg", help="테스트 이미지 경로")


@events.test_start.add_listener
def on_test_start(environment, **kwargs):
    """테스트 시작 시"""
    print("=" * 80)
    print("Performance Test Starting")
    print(f"Host: {environment.host}")
    print(f"Store ID: {STORE_ID}")
    print(f"Image Path: {TEST_IMAGE_PATH}")
    print("=" * 80)


@events.test_stop.add_listener
def on_test_stop(environment, **kwargs):
    """테스트 종료 시"""
    print("=" * 80)
    print("Performance Test Completed")
    print("=" * 80)


"""
사용법:

1. 동기 블로킹 버전 테스트 (70 users, 톰캣 스레드 20개 고갈 재현)
   locust -f locustfile.py --host=http://localhost:8080 \\
          --users 70 --spawn-rate 70 \\
          --run-time 3m \\
          --auth-token "your-token" \\
          --store-id "1" \\
          --test-image "./test-menu.jpg" \\
          MenuExtractionSyncUser

   예상: 26% 실패율, 평균 12-27초, ReadTimeout 발생

2. 단순 비동기 버전 테스트 (70 users, 비동기 스레드 풀 10개 고갈 재현)
   locust -f locustfile.py --host=http://localhost:8080 \\
          --users 70 --spawn-rate 70 \\
          --run-time 3m \\
          --auth-token "your-token" \\
          --store-id "1" \\
          --test-image "./test-menu.jpg" \\
          MenuExtractionSimpleAsyncUser

   예상: TaskRejectedException 발생 (비동기 스레드 풀 고갈), 60% 실패율

3. 비동기 논블로킹 버전 테스트 (70 users)
   locust -f locustfile.py --host=http://localhost:8080 \\
          --users 70 --spawn-rate 70 \\
          --run-time 3m \\
          --auth-token "your-token" \\
          --store-id "1" \\
          --test-image "./test-menu.jpg" \\
          MenuExtractionAsyncUser

   예상: 0% 실패율, 초기 응답 50-100ms, 전체 완료 3-5초

4. Web UI로 실행 (추천)
   locust -f locustfile.py --host=http://localhost:8080
   브라우저에서 http://localhost:8089 접속

5. 환경 변수 사용
   export AUTH_TOKEN="your-token"
   export STORE_ID="1"
   export TEST_IMAGE_PATH="./test-menu.jpg"
   locust -f locustfile.py --host=http://localhost:8080

주요 측정 지표:
- 평균 응답 시간 (Average Response Time)
- 최대 응답 시간 (Max Response Time)
- RPS (Requests Per Second)
- 실패율 (Failure Rate)
- P95, P99 응답 시간

비교 요약:
┌─────────────────────┬───────────────┬─────────────────┬──────────────────┐
│ 방식                │ 스레드 풀     │ 예상 실패율     │ 주요 에러        │
├─────────────────────┼───────────────┼─────────────────┼──────────────────┤
│ 동기 블로킹         │ 톰캣 20개     │ 26%             │ ReadTimeout      │
│ 단순 비동기         │ 비동기 10개   │ 60%             │ TaskRejected     │
│ 비동기 논블로킹     │ 리소스 효율적 │ 0%              │ 없음             │
└─────────────────────┴───────────────┴─────────────────┴──────────────────┘
"""
