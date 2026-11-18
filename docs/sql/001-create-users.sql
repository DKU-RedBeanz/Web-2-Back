-- REVIEW REQUIRED: mentor confirmation before applying to personal MySQL.
-- Select redbeanz first. Never drop an existing table to apply this script.
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    login_id VARCHAR(30) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    nickname VARCHAR(30) NOT NULL,
    role VARCHAR(16) NOT NULL DEFAULT 'USER',
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_login_id UNIQUE (login_id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('USER', 'ADMIN'))
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs;
