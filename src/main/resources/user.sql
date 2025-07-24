CREATE TABLE users (
    apriID BIGINT AUTO_INCREMENT PRIMARY KEY,
    kakao_user_id BIGINT UNIQUE, 
    user_name VARCHAR(100),
    gender VARCHAR(10),
    birth_year INT,
    registration_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    thumbnail_url TEXT,
    nickname VARCHAR(100)
)