-- 실습에 사용할 로컬 데이터베이스를 선택한 뒤 실행합니다.

CREATE TABLE IF NOT EXISTS restaurants (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone_number VARCHAR(30),
    description VARCHAR(1000),
    thumbnail_url VARCHAR(500),
    PRIMARY KEY (id)
)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO restaurants (
    name,
    address,
    phone_number,
    description,
    thumbnail_url
)
SELECT
    '레드빈즈 식당',
    '경기도 용인시 수지구 죽전로 1',
    '031-111-1111',
    '김치찌개를 판매하는 식당입니다.',
    'https://example.com/redbeanz.jpg'
WHERE NOT EXISTS (
    SELECT 1
    FROM restaurants
    WHERE name = '레드빈즈 식당'
      AND address = '경기도 용인시 수지구 죽전로 1'
);

INSERT INTO restaurants (
    name,
    address,
    phone_number,
    description,
    thumbnail_url
)
SELECT
    '블루빈즈 카페',
    '경기도 용인시 수지구 죽전로 2',
    '031-222-2222',
    '커피와 디저트를 판매하는 카페입니다.',
    'https://example.com/bluebeanz.jpg'
WHERE NOT EXISTS (
    SELECT 1
    FROM restaurants
    WHERE name = '블루빈즈 카페'
      AND address = '경기도 용인시 수지구 죽전로 2'
);
