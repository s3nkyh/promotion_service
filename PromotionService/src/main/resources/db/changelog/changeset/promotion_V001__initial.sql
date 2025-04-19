CREATE TABLE promotion (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    impressions BIGINT NOT NULL,
    duration_days INT NOT NULL,
    usd_price DECIMAL(10,2) NOT NULL,
    priority INT NOT NULL CHECK (priority BETWEEN 1 AND 10),
    target_type VARCHAR(255) NOT NULL,
    is_unlimited BOOLEAN DEFAULT FALSE
);