-- V10__add_site_link_unique_constraint.sql

-- storeName의 unique constraint 제거
ALTER TABLE stores DROP INDEX store_name;

-- 모든 링크 컬럼을 TEXT → VARCHAR(255)로 변경
ALTER TABLE stores MODIFY COLUMN baemin_link VARCHAR(255);
ALTER TABLE stores MODIFY COLUMN yogiyo_link VARCHAR(255);
ALTER TABLE stores MODIFY COLUMN coupangeats_link VARCHAR(255);
ALTER TABLE stores MODIFY COLUMN naver_map_link VARCHAR(255);
ALTER TABLE stores MODIFY COLUMN kakao_map_link VARCHAR(255);
ALTER TABLE stores MODIFY COLUMN instagram_link VARCHAR(255);
ALTER TABLE stores MODIFY COLUMN kakao_talk_link VARCHAR(255);
ALTER TABLE stores MODIFY COLUMN site_link VARCHAR(255);

-- site_link에 unique constraint 추가
ALTER TABLE stores ADD CONSTRAINT uk_site_link UNIQUE (site_link);
