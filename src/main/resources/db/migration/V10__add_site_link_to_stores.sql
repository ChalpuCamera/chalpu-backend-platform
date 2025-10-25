ALTER TABLE stores ADD COLUMN site_link VARCHAR(100) NOT NULL UNIQUE;

CREATE INDEX idx_stores_site_link ON stores(site_link);
