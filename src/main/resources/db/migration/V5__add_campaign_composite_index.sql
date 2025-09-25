-- 활성 캠페인 조회 성능 개선을 위한 복합 인덱스 추가
-- CampaignRepository의 findActiveByStoreAndFoodItem 쿼리 최적화

-- 매장과 음식별 활성 캠페인 조회를 위한 복합 인덱스
CREATE INDEX idx_campaign_active_lookup
ON campaign(store_id, food_item_id, status, is_active, start_date, end_date);

-- 매장별 활성 캠페인 카운트 조회를 위한 인덱스
CREATE INDEX idx_campaign_store_active
ON campaign(store_id, status, is_active);

-- 캠페인 상태별 조회를 위한 인덱스
CREATE INDEX idx_campaign_status
ON campaign(status)
WHERE is_active = true;

-- 날짜 범위 기반 캠페인 조회를 위한 인덱스
CREATE INDEX idx_campaign_date_range
ON campaign(start_date, end_date)
WHERE status = 'ACTIVE' AND is_active = true;