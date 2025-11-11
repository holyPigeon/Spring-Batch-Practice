-- 1. 휴면 대상 (1년 1일 전 로그인)
INSERT INTO users (username, status, last_login_date)
VALUES ('oldUser', 'ACTIVE', CURRENT_TIMESTAMP - (1 YEAR + 1 DAY));

-- 2. 휴면 대상 (2년 전 로그인)
INSERT INTO users (username, status, last_login_date)
VALUES ('veryOldUser', 'ACTIVE', CURRENT_TIMESTAMP - 2 YEAR);

-- 3. 휴면 대상 아님 (10일 전 로그인)
INSERT INTO users (username, status, last_login_date)
VALUES ('activeUser', 'ACTIVE', CURRENT_TIMESTAMP - 10 DAY);

-- 4. 휴면 대상 아님 (이미 휴면 상태)
INSERT INTO users (username, status, last_login_date)
VALUES ('alreadyDormantUser', 'DORMANT', CURRENT_TIMESTAMP - 2 YEAR);