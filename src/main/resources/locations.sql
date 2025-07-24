CREATE TABLE locations (
    location_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_id BIGINT UNIQUE, 
    loc_x VARCHAR(100),
    loc_y VARCHAR(100),
    
)