-- 100만 번의 재귀(반복)를 허용하도록 세션 설정을 변경
SET SESSION cte_max_recursion_depth = 1000000;

TRUNCATE TABLE users;

-- 100만 건의 숫자(cte)를 생성하고,
-- 그 숫자(n)를 기반으로 90%는 휴면 대상, 10%는 활성 대상으로 나누어
-- "users" 테이블에 '단 1번의 쿼리로' INSERT 합니다.

INSERT INTO users (username, status, last_login_date)
WITH RECURSIVE cte (n) AS
                   (
                       SELECT 1 -- 1. 시작 숫자
                       UNION ALL
                       SELECT n + 1 FROM cte WHERE n < 1000000 -- 2. 100만까지 1씩 증가
                   )
SELECT
    -- 3. 10% (n이 10의 배수)는 'activeUser'로 생성
    CASE
        WHEN (n % 10 = 0) THEN CONCAT('activeUser', n)
        ELSE CONCAT('dormantTargetUser', n)
        END AS username,

    'ACTIVE' AS status,

    -- 4. 10%는 10일 전 로그인, 90%는 1년 1일 전 로그인
    CASE
        WHEN (n % 10 = 0) THEN NOW() - INTERVAL 10 DAY
        ELSE NOW() - INTERVAL 1 YEAR - INTERVAL 1 DAY
        END AS last_login_date

FROM cte;