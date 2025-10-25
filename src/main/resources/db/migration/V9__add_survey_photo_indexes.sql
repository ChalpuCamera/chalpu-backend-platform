-- SurveyAnswer와 Photo 관련 테이블 성능 최적화를 위한 인덱스 추가

-- ===== SurveyAnswer 테이블 인덱스 =====

-- 1. 피드백별 답변 조회 최적화
-- findByFeedbackIdOrderByQuestionId 쿼리 지원
CREATE INDEX idx_survey_answer_feedback
ON survey_answers(feedback_id, question_id);

-- 2. JAR 분석용 인덱스
-- 특정 질문의 숫자형 답변 집계 및 분석
CREATE INDEX idx_survey_answer_jar
ON survey_answers(question_id, numeric_value)
WHERE numeric_value IS NOT NULL;

-- 3. 텍스트 답변이 있는 경우 조회
-- 사장님께 한마디 등 텍스트 답변 조회
CREATE INDEX idx_survey_answer_text
ON survey_answers(question_id, feedback_id)
WHERE answer_text IS NOT NULL;

-- 4. 질문별 통계 집계용 인덱스
CREATE INDEX idx_survey_answer_stats
ON survey_answers(question_id, numeric_value, created_at);

-- ===== FeedbackPhoto 테이블 인덱스 =====

-- 1. 피드백별 사진 조회 최적화
-- 특정 피드백의 모든 사진 조회
CREATE INDEX idx_feedback_photo_feedback
ON feedback_photos(feedback_id);

-- 2. S3 키로 사진 조회
CREATE INDEX idx_feedback_photo_s3key
ON feedback_photos(s3_key);

-- ===== Photo 테이블 인덱스 =====

-- 1. 음식별 사진 조회 최적화
-- 특정 음식의 대표 사진 및 모든 사진 조회
CREATE INDEX idx_photo_food_active
ON photos(food_item_id, is_active, is_featured);

-- 2. 대표 사진 조회 최적화
-- 메뉴 목록에서 대표 사진만 빠르게 조회
CREATE INDEX idx_photo_featured
ON photos(is_featured, is_active)
WHERE is_featured = true AND is_active = true;

-- 3. 임시 사진 정리용 인덱스
-- 오래된 임시 사진 삭제 배치 작업
CREATE INDEX idx_photo_temporary
ON photos(is_temporary, created_at)
WHERE is_temporary = true;

-- 4. S3 키로 사진 조회
CREATE INDEX idx_photo_s3key
ON photos(s3_key);

-- ===== Survey 테이블 인덱스 =====

-- 1. 활성 설문 조회 최적화
CREATE INDEX idx_survey_active
ON surveys(is_active, created_at DESC)
WHERE is_active = true;