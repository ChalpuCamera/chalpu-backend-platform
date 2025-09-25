-- Campaign과 CustomerFeedback 연관관계 추가 마이그레이션

-- 1. feedback 테이블에 campaign_id 컬럼 추가
ALTER TABLE feedback
ADD COLUMN campaign_id BIGINT,
ADD CONSTRAINT fk_feedback_campaign
    FOREIGN KEY (campaign_id) REFERENCES campaigns(campaign_id)
    ON DELETE SET NULL;

-- 2. campaign_id에 대한 인덱스 추가 (성능 최적화)
CREATE INDEX idx_feedback_campaign ON feedback(campaign_id);
CREATE INDEX idx_feedback_campaign_active ON feedback(campaign_id, is_active);

-- 3. campaigns 테이블에 current_feedback_count 컬럼 추가
ALTER TABLE campaigns
ADD COLUMN current_feedback_count INT NOT NULL DEFAULT 0;

-- 4. 기존 데이터 마이그레이션 (옵션)
-- 기존 캠페인 기간 내 피드백을 연결하고 카운트 업데이트
UPDATE campaigns c
SET current_feedback_count = (
    SELECT COUNT(*)
    FROM feedback f
    WHERE f.store_id = c.store_id
    AND f.food_id = c.food_item_id
    AND f.created_at BETWEEN c.start_date AND c.end_date
    AND f.is_active = true
);

-- 5. 기존 피드백에 캠페인 연결 (옵션)
-- 캠페인 기간 내 생성된 피드백을 해당 캠페인에 연결
UPDATE feedback f
SET campaign_id = (
    SELECT c.campaign_id
    FROM campaigns c
    WHERE c.store_id = f.store_id
    AND c.food_item_id = f.food_id
    AND c.status = 'ACTIVE'
    AND f.created_at BETWEEN c.start_date AND c.end_date
    LIMIT 1
)
WHERE f.campaign_id IS NULL
AND EXISTS (
    SELECT 1 FROM campaigns c
    WHERE c.store_id = f.store_id
    AND c.food_item_id = f.food_id
    AND c.status = 'ACTIVE'
    AND f.created_at BETWEEN c.start_date AND c.end_date
);

-- 6. 통계 확인용 쿼리 (실행 후 삭제 가능)
-- SELECT
--     c.campaign_id,
--     c.name as campaign_name,
--     c.current_feedback_count,
--     COUNT(f.feedback_id) as actual_count
-- FROM campaigns c
-- LEFT JOIN feedback f ON c.campaign_id = f.campaign_id AND f.is_active = true
-- GROUP BY c.campaign_id, c.name, c.current_feedback_count;