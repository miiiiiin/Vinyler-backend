-- Vinyl 엔티티에서 제거된 컬럼(notes, status, uri) 정리

ALTER TABLE vinyls DROP COLUMN IF EXISTS notes;
ALTER TABLE vinyls DROP COLUMN IF EXISTS status;
ALTER TABLE vinyls DROP COLUMN IF EXISTS uri;
