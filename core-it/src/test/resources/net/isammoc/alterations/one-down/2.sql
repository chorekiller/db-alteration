-- !Ups
ALTER TABLE foo
    ADD COLUMN baz INT;

-- !Downs
ALTER TABLE foo
    DROP COLUMN IF EXISTS baz;
