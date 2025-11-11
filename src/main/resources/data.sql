-- 1. 휴면 대상 (1년 1일 전 로그인)
-- INTERVAL 1 YEAR -> '1' YEAR, INTERVAL 1 DAY -> '1' DAY
INSERT INTO users (username, status, last_login_date)
VALUES ('oldUser', 'ACTIVE', CURRENT_TIMESTAMP - '1' YEAR - '1' DAY);

-- 2. 휴면 대상 (2년 전 로그인)
-- INTERVAL 2 YEAR -> '2' YEAR
INSERT INTO users (username, status, last_login_date)
VALUES ('veryOldUser', 'ACTIVE', CURRENT_TIMESTAMP - '2' YEAR);

-- 3. 휴면 대상 아님 (10일 전 로그인)
-- INTERVAL 10 DAY -> '10' DAY
INSERT INTO users (username, status, last_login_date)
VALUES ('activeUser', 'ACTIVE', CURRENT_TIMESTAMP - '10' DAY);

-- 4. 휴면 대상 아님 (이미 휴면 상태)
-- INTERVAL 2 YEAR -> '2' YEAR
INSERT INTO users (username, status, last_login_date)
VALUES ('alreadyDormantUser', 'DORMANT', CURRENT_TIMESTAMP - '2' YEAR);