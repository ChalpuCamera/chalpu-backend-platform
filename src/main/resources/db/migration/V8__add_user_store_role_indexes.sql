-- UserStoreRole 테이블 성능 최적화를 위한 인덱스 추가
-- 매장-사용자 권한 관리 및 조회 최적화

-- 1. 사용자의 매장 역할 조회 최적화
-- findByUserIdAndIsActiveTrue 쿼리 지원
-- 사용자가 속한 모든 매장 목록 조회
CREATE INDEX idx_user_store_role_user
ON user_store_roles(user_id, is_active);

-- 2. 매장의 멤버 목록 조회 최적화
-- findByStoreIdAndIsActiveTrue 쿼리 지원
-- 특정 매장의 모든 멤버 조회
CREATE INDEX idx_user_store_role_store
ON user_store_roles(store_id, is_active);

-- 3. 사용자와 매장의 고유 역할 확인 (Unique Partial Index)
-- 한 사용자는 한 매장에 하나의 활성 역할만 가능
CREATE UNIQUE INDEX idx_user_store_unique
ON user_store_roles(user_id, store_id)
WHERE is_active = true;

-- 4. 역할 타입별 조회 최적화
-- 특정 역할(OWNER, MANAGER, EMPLOYEE)을 가진 사용자 검색
CREATE INDEX idx_user_store_role_type
ON user_store_roles(role_type, is_active)
WHERE is_active = true;

-- 5. 사용자-매장-역할 복합 조회 최적화
-- 권한 검증 시 빠른 조회를 위한 복합 인덱스
CREATE INDEX idx_user_store_role_composite
ON user_store_roles(user_id, store_id, role_type, is_active);

-- 6. 생성일 기준 정렬 조회
-- 최근 추가된 멤버 조회 등
CREATE INDEX idx_user_store_role_created
ON user_store_roles(created_at DESC)
WHERE is_active = true;