CREATE TABLE test_table
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL,
    surname    VARCHAR(100) NOT NULL,
    created_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);