-- SRID 4326 means "WGS84 Coordinate System" which is same as gps coordinates for most of systems.



-- 1) Location sources (Kakao/Naver/etc.)
CREATE TABLE IF NOT EXISTS location_sources (
  id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  UNIQUE KEY uq_source_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) Per-user, ephemeral location cache

CREATE TABLE IF NOT EXISTS user_locations (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  apri_id      BIGINT NOT NULL,  -- apri_id 
  source_id    BIGINT NULL,   -- source type,
  external_id  VARCHAR(128) NULL,  -- e.g.) content_id from TOUR_API DB
  name         VARCHAR(300) NULL,
  address      VARCHAR(500) NULL,
  point        POINT SRID 4326 NOT NULL,
  details_json JSON NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at   TIMESTAMP NOT NULL,
  FOREIGN KEY (source_id) REFERENCES location_sources(id),
  FOREIGN KEY (apri_id) REFERENCES users(apri_id) ON DELETE CASCADE, -- 

   -- Needed for composite FKs from daily_plan_items (apri_id, location_id)
  UNIQUE KEY uq_user_src_xid (apri_id, source_id, external_id),
  UNIQUE KEY uq_ul_apri_id_id (apri_id, id),
  SPATIAL INDEX spx_point (point),
  INDEX (apri_id, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 3) Per-user, ephemeral routes


CREATE TABLE IF NOT EXISTS user_routes (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  apri_id      BIGINT NOT NULL,  -- apri_id 
  source_id    BIGINT NULL,
  external_id  VARCHAR(128) NULL,
  name         VARCHAR(300) NULL,
  start_point  POINT SRID 4326 NOT NULL, -- Spatial index must be "NOT NULL"
  end_point    POINT SRID 4326 NOT NULL,
  path         LINESTRING SRID 4326 NOT NULL,  -- Full combined path of ApriPathDTO
  distance_m   DOUBLE NULL,   -- Total distance
  duration_s   INT NULL,  -- Total duration

  details_json JSON NULL,
  segments_json JSON NULL,  -- ApriPathDTO object stored as a json.


  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at   TIMESTAMP NOT NULL,
  FOREIGN KEY (source_id) REFERENCES location_sources(id),
  FOREIGN KEY (apri_id) REFERENCES users(apri_id) ON DELETE CASCADE, -- <-- ADD THIS
  UNIQUE KEY uq_user_route_src_xid (apri_id, source_id, external_id),

    -- Needed for composite FKs from daily_plan_items (apri_id, route_id)
  UNIQUE KEY uq_ur_apri_id_id (apri_id, id),
  SPATIAL INDEX spx_path (path),
  SPATIAL INDEX spx_start (start_point),
  SPATIAL INDEX spx_end (end_point),
  INDEX (apri_id, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 4) Plans   consist of 0..n Daily Plans.

CREATE TABLE IF NOT EXISTS total_plans (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  apri_id     BIGINT NOT NULL,  -- apri_id 
  title       VARCHAR(200) NOT NULL,
  start_date  DATE NULL,
  end_date    DATE NULL,
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  use_yn      VARCHAR(1) NOT NULL,  -- 20250820, add columns 'y','n'
  FOREIGN KEY (apri_id) REFERENCES users(apri_id) ON DELETE CASCADE, 
  INDEX (apri_id),
  INDEX (start_date),
  INDEX (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 4.1) Daily Plans
CREATE TABLE IF NOT EXISTS daily_plans (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  total_plan_id BIGINT NOT NULL,
  apri_id       BIGINT NOT NULL,  -- apri_id 
  day_index     INT NOT NULL,  -- 1..N
  date          DATE NULL,
  FOREIGN KEY (total_plan_id) REFERENCES total_plans(id) ON DELETE CASCADE,
  FOREIGN KEY (apri_id) REFERENCES users(apri_id) ON DELETE CASCADE, -- <-- ADD THIS
  UNIQUE KEY uq_daily_idx (total_plan_id, day_index),
    -- Needed for composite FK from daily_plan_items (apri_id, daily_plan_id)
  UNIQUE KEY uq_dp_apri_id_id (apri_id, id),
  INDEX (apri_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 5) Ordered items per day (locations & routes)
CREATE TABLE IF NOT EXISTS daily_plan_items (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  apri_id        BIGINT NOT NULL, -- apri_id 
  daily_plan_id  BIGINT NOT NULL,
  item_type      ENUM('location','route') NOT NULL,
  location_id    BIGINT NULL,
  route_id       BIGINT NULL,
  position       INT NOT NULL,
  notes          VARCHAR(500) NULL,
  start_time     DATETIME NOT NULL,  -- 20250820, add columns, Using DATETIME instead of DATE to avoid problems of late-night events such as 01:00
  end_time       DATETIME NOT NULL,   -- 20250820, add columns
  FOREIGN KEY (apri_id) REFERENCES users(apri_id) ON DELETE CASCADE, -- <-- ADD THIS

    -- Enforce day ownership (same user)
  CONSTRAINT fk_item_day FOREIGN KEY (apri_id, daily_plan_id)
    REFERENCES daily_plans(apri_id, id) ON DELETE CASCADE,

      -- Enforce that referenced location/route belongs to same user
  CONSTRAINT fk_item_loc FOREIGN KEY (apri_id, location_id)
    REFERENCES user_locations(apri_id, id) ON DELETE RESTRICT,
  CONSTRAINT fk_item_route FOREIGN KEY (apri_id, route_id)
    REFERENCES user_routes(apri_id, id) ON DELETE RESTRICT,
  CONSTRAINT chk_one_ref CHECK (
    (item_type='location' AND location_id IS NOT NULL AND route_id IS NULL) OR
    (item_type='route'    AND route_id    IS NOT NULL AND location_id IS NULL)
  ),
  UNIQUE KEY uq_item_order (daily_plan_id, position),
  INDEX (apri_id, daily_plan_id, item_type),
  INDEX (location_id),
  INDEX (route_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;