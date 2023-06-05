-- !Ups
CREATE TABLE foo
(
    id  INT PRIMARY KEY,
    bar varchar
);

-- !Downs
DROP TABLE IF EXISTS foo;
