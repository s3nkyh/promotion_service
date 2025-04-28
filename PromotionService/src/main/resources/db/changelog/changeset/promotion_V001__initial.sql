CREATE TABLE promotion
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255)   NOT NULL UNIQUE,
    impressions   BIGINT         NOT NULL,
    duration_days INT            NOT NULL,
    usd_price     DECIMAL(10, 2) NOT NULL,
    priority      INT            NOT NULL CHECK (priority BETWEEN 1 AND 10),
    target_type   VARCHAR(255)   NOT NULL
);
INSERT INTO promotion (name,
                       impressions,
                       duration_days,
                       usd_price,
                       priority,
                       target_type)
VALUES ('Basic', 1000, 7, 9.99, 3, 'PROFILE'),
       ('Plus', 5000, 14, 19.99, 5, 'PROFILE'),
       ('Premium', 15000, 30, 49.99, 8, 'PROFILE');

CREATE TABLE active_promotions
(
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id               BIGINT,
    target_type           VARCHAR(255) NOT NULL,
    target_id             BIGINT,
    promotion             BIGINT       NOT NULL,
    remaining_impressions BIGINT       NOT NULL,
    start_time            TIMESTAMP    NOT NULL,
    priority              INT          NOT NULL,
    end_time              TIMESTAMP    NOT NULL,
    CONSTRAINT fk_promotion
        FOREIGN KEY (promotion)
            REFERENCES promotion (id)
);