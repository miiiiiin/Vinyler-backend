-- Vinyl 엔티티 경량화 (Discogs API ToS 대응): reviews_count 컬럼 추가, 더 이상 영구 저장하지 않는 자식 테이블 제거

ALTER TABLE vinyls ADD COLUMN IF NOT EXISTS reviews_count BIGINT DEFAULT 0;

UPDATE vinyls v
SET reviews_count = sub.cnt
FROM (SELECT vinyl_id, COUNT(*) AS cnt FROM reviews GROUP BY vinyl_id) sub
WHERE v.vinyl_id = sub.vinyl_id;

UPDATE vinyls SET reviews_count = 0 WHERE reviews_count IS NULL;

DROP TABLE IF EXISTS format_descriptions;
DROP TABLE IF EXISTS images;
DROP TABLE IF EXISTS tracklist;
DROP TABLE IF EXISTS format;
DROP TABLE IF EXISTS artist_detail;
DROP TABLE IF EXISTS video;
