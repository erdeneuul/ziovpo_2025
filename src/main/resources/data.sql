INSERT INTO licenses (activation_code, status, expires_at, created_at)
VALUES
  ('TEST-1111-2222-3333', 'ACTIVE',  NOW() + INTERVAL '1 year',  NOW()),
  ('TEST-AAAA-BBBB-CCCC', 'ACTIVE',  NOW() + INTERVAL '1 year',  NOW()),
  ('TEST-EXPIRED-0000',   'EXPIRED', NOW() - INTERVAL '1 day',   NOW()),
  ('TEST-BLOCKED-0000',   'BLOCKED', NOW() + INTERVAL '1 year',  NOW())
ON CONFLICT (activation_code) DO NOTHING;
