INSERT INTO promotion (
    name,
    impressions,
    duration_days,
    usd_price,
    priority,
    target_type,
    is_unlimited
) VALUES
    ('Basic', 1000, 7, 9.99, 3, 'PROFILE', false),
    ('Plus', 5000, 14, 19.99, 5, 'PROFILE', false),
    ('Premium', 15000, 30, 49.99, 8, 'PROFILE', false),
    ('VIP', 0, 30, 99.99, 10, 'PROFILE', true);