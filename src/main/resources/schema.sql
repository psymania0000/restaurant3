-- Modify users table to add default value for phone column
ALTER TABLE users MODIFY COLUMN phone VARCHAR(20) NOT NULL DEFAULT '010-0000-0000';

-- Create restaurants table if not exists
CREATE TABLE IF NOT EXISTS restaurants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    description TEXT,
    business_hours VARCHAR(100),
    category VARCHAR(50) NOT NULL,
    image_url VARCHAR(200),
    manager_email VARCHAR(100) UNIQUE,
    admin_email VARCHAR(100),
    is_open BOOLEAN DEFAULT TRUE,
    max_capacity INT DEFAULT 0,
    reservation_interval INT DEFAULT 30,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create menus table if not exists
CREATE TABLE IF NOT EXISTS menus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DOUBLE NOT NULL,
    category VARCHAR(50) NOT NULL,
    available BOOLEAN DEFAULT TRUE,
    image_url VARCHAR(200),
    restaurant_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
);

-- Remember Me 토큰 저장 테이블
CREATE TABLE IF NOT EXISTS persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL
);

-- notices 테이블 수정
ALTER TABLE notices DROP FOREIGN KEY FKdcx7eafm2avpoafgx6otyvd79;
ALTER TABLE notices ADD CONSTRAINT FK_notices_users FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE;

-- user_restaurant 테이블 수정
ALTER TABLE user_restaurant DROP FOREIGN KEY IF EXISTS FK_user_restaurant_user;
ALTER TABLE user_restaurant DROP FOREIGN KEY IF EXISTS FK_user_restaurant_restaurant;
ALTER TABLE user_restaurant ADD CONSTRAINT FK_user_restaurant_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE user_restaurant ADD CONSTRAINT FK_user_restaurant_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE; 