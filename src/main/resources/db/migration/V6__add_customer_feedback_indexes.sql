-- CustomerFeedback 테이블 성능 최적화를 위한 인덱스 추가
-- 실제 쿼리 패턴 기반으로 필수 인덱스만 생성

-- 1. 사용자별 피드백 조회 (가장 빈번한 조회)
-- findByUserIdAndIsActiveTrueOrderByCreatedAtDesc
CREATE INDEX idx_feedback_user_active
ON feedback(user_id, is_active, created_at DESC)
WHERE is_active = true;

-- 2. 매장별 피드백 조회 (관리자 대시보드)
-- findByStoreIdAndIsActiveTrueOrderByCreatedAtDesc
CREATE INDEX idx_feedback_store_active
ON feedback(store_id, is_active, created_at DESC)
WHERE is_active = true;

-- 3. 캠페인별 피드백 조회 (캠페인 성과 분석)
-- findByCampaignIdAndIsActiveTrueOrderByCreatedAtDesc
CREATE INDEX idx_feedback_campaign_active
ON feedback(campaign_id, is_active, created_at DESC)
WHERE is_active = true AND campaign_id IS NOT NULL;

-- 참고: food_id, store_id는 외래키로 이미 인덱스 존재
-- 복합 인덱스는 실제 성능 이슈 발생 시 추가