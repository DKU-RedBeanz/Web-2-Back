USE redbeanz;

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

CREATE TABLE IF NOT EXISTS restaurant_menus (
    id BIGINT NOT NULL AUTO_INCREMENT,
    restaurant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    price INT NOT NULL,
    description VARCHAR(500),
    image_url VARCHAR(500),
    available BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_restaurant_menus_restaurant
        FOREIGN KEY (restaurant_id)
        REFERENCES restaurants (id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_restaurant_menus_price
        CHECK (price >= 0)
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

SET @restaurant1_id = (
    SELECT id
    FROM restaurants
    WHERE name = '레드빈즈 식당'
    ORDER BY id
    LIMIT 1
);

SET @restaurant2_id = (
    SELECT id
    FROM restaurants
    WHERE name = '블루빈즈 카페'
    ORDER BY id
    LIMIT 1
);

INSERT INTO restaurant_menus (
    restaurant_id,
    name,
    price,
    description,
    image_url,
    available
)
SELECT
    @restaurant1_id,
    '김치찌개',
    9000,
    '돼지고기가 들어간 김치찌개입니다.',
    'https://example.com/kimchi-stew.jpg',
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM restaurant_menus
    WHERE restaurant_id = @restaurant1_id
      AND name = '김치찌개'
);

INSERT INTO restaurant_menus (
    restaurant_id,
    name,
    price,
    description,
    image_url,
    available
)
SELECT
    @restaurant2_id,
    '아메리카노',
    4500,
    '고소한 원두로 만든 아메리카노입니다.',
    'https://example.com/americano.jpg',
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM restaurant_menus
    WHERE restaurant_id = @restaurant2_id
      AND name = '아메리카노'
);

SELECT * FROM restaurants ORDER BY id;
SELECT * FROM restaurant_menus ORDER BY id;