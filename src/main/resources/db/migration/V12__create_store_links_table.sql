-- Create store_links table
CREATE TABLE store_links (
    link_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    link_type VARCHAR(50) NOT NULL,
    custom_label VARCHAR(50),
    url VARCHAR(500) NOT NULL,
    display_order INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_store_display_order (store_id, display_order),
    FOREIGN KEY (store_id) REFERENCES stores(store_id) ON DELETE CASCADE
);

-- Migrate existing data from stores table to store_links
INSERT INTO store_links (store_id, link_type, url, display_order, created_at, updated_at)
SELECT store_id, 'BAEMIN', baemin_link, 0, NOW(), NOW()
FROM stores
WHERE baemin_link IS NOT NULL AND baemin_link != '';

INSERT INTO store_links (store_id, link_type, url, display_order, created_at, updated_at)
SELECT store_id, 'YOGIYO', yogiyo_link, 1, NOW(), NOW()
FROM stores
WHERE yogiyo_link IS NOT NULL AND yogiyo_link != '';

INSERT INTO store_links (store_id, link_type, url, display_order, created_at, updated_at)
SELECT store_id, 'COUPANGEATS', coupangeats_link, 2, NOW(), NOW()
FROM stores
WHERE coupangeats_link IS NOT NULL AND coupangeats_link != '';

INSERT INTO store_links (store_id, link_type, url, display_order, created_at, updated_at)
SELECT store_id, 'NAVER_MAP', naver_map_link, 3, NOW(), NOW()
FROM stores
WHERE naver_map_link IS NOT NULL AND naver_map_link != '';

INSERT INTO store_links (store_id, link_type, url, display_order, created_at, updated_at)
SELECT store_id, 'KAKAO_MAP', kakao_map_link, 4, NOW(), NOW()
FROM stores
WHERE kakao_map_link IS NOT NULL AND kakao_map_link != '';

INSERT INTO store_links (store_id, link_type, url, display_order, created_at, updated_at)
SELECT store_id, 'INSTAGRAM', instagram_link, 5, NOW(), NOW()
FROM stores
WHERE instagram_link IS NOT NULL AND instagram_link != '';

INSERT INTO store_links (store_id, link_type, url, display_order, created_at, updated_at)
SELECT store_id, 'KAKAO_TALK', kakao_talk_link, 6, NOW(), NOW()
FROM stores
WHERE kakao_talk_link IS NOT NULL AND kakao_talk_link != '';

INSERT INTO store_links (store_id, link_type, url, display_order, created_at, updated_at)
SELECT store_id, 'GOOGLE_MAPS', google_maps_link, 7, NOW(), NOW()
FROM stores
WHERE google_maps_link IS NOT NULL AND google_maps_link != '';

INSERT INTO store_links (store_id, link_type, url, display_order, created_at, updated_at)
SELECT store_id, 'DDANGYO', ddangyo_link, 8, NOW(), NOW()
FROM stores
WHERE ddangyo_link IS NOT NULL AND ddangyo_link != '';

INSERT INTO store_links (store_id, link_type, url, display_order, created_at, updated_at)
SELECT store_id, 'DAANGN', daangn_link, 9, NOW(), NOW()
FROM stores
WHERE daangn_link IS NOT NULL AND daangn_link != '';

INSERT INTO store_links (store_id, link_type, url, display_order, created_at, updated_at)
SELECT store_id, 'SITE', site_link, 10, NOW(), NOW()
FROM stores
WHERE site_link IS NOT NULL AND site_link != '';

-- Reorder display_order for each store to be sequential (0, 1, 2, ...)
SET @row_number = 0;
SET @current_store = NULL;

UPDATE store_links sl
JOIN (
    SELECT
        link_id,
        @row_number := IF(@current_store = store_id, @row_number + 1, 0) AS new_order,
        @current_store := store_id
    FROM store_links
    ORDER BY store_id, display_order
) AS numbered ON sl.link_id = numbered.link_id
SET sl.display_order = numbered.new_order;

-- Drop old link columns from stores table
ALTER TABLE stores
DROP COLUMN baemin_link,
DROP COLUMN yogiyo_link,
DROP COLUMN coupangeats_link,
DROP COLUMN naver_map_link,
DROP COLUMN kakao_map_link,
DROP COLUMN instagram_link,
DROP COLUMN kakao_talk_link,
DROP COLUMN google_maps_link,
DROP COLUMN ddangyo_link,
DROP COLUMN daangn_link,
DROP COLUMN site_link;
