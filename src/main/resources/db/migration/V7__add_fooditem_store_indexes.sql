-- FoodItem과 Store 테이블 성능 최적화를 위한 인덱스 추가

-- ===== FoodItem 테이블 인덱스 =====

-- 1. 매장별 활성 음식 조회 최적화
-- 매장의 메뉴 목록 조회 시 사용
CREATE INDEX idx_food_store_active
ON food_items(store_id, is_active, food_name);

-- 2. 카테고리별 음식 조회 최적화 (Partial Index)
-- 카테고리별 메뉴 필터링 시 사용
CREATE INDEX idx_food_category
ON food_items(category, is_active)
WHERE is_active = true;

-- 3. 가격 범위 조회를 위한 인덱스
-- 가격대별 메뉴 검색 시 사용
CREATE INDEX idx_food_price
ON food_items(price, is_active)
WHERE is_active = true;

-- ===== Store 테이블 인덱스 =====

-- 1. 활성 매장 조회 최적화
-- 매장 목록 및 검색 시 사용
CREATE INDEX idx_store_active
ON stores(is_active, store_name);

-- 2. 주소 기반 매장 검색 최적화
-- 지역별 매장 검색 시 사용
CREATE INDEX idx_store_address
ON stores(address, is_active)
WHERE is_active = true;

-- 3. 매장명 검색 최적화 (부분 일치 검색)
-- LIKE 검색 성능 개선
CREATE INDEX idx_store_name_pattern
ON stores(store_name varchar_pattern_ops)
WHERE is_active = true;