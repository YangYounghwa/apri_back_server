-- 1) Location sources (Kakao/Naver/etc.)
CREATE TABLE IF NOT EXISTS location_sources (
  id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  UNIQUE KEY uq_source_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) Per-user, ephemeral location cache
CREATE TABLE IF NOT EXISTS user_locations (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id      BIGINT NOT NULL,
  source_id    BIGINT NULL,
  external_id  VARCHAR(128) NULL,
  name         VARCHAR(300) NULL,
  address      VARCHAR(500) NULL,
  point        POINT SRID 4326 NULL,
  details_json JSON NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at   TIMESTAMP NOT NULL,
  FOREIGN KEY (source_id) REFERENCES location_sources(id),
  UNIQUE KEY uq_user_src_xid (user_id, source_id, external_id),
  -- Needed for composite FKs from daily_plan_items (user_id, location_id)
  UNIQUE KEY uq_ul_user_id_id (user_id, id),
  SPATIAL INDEX spx_point (point),
  INDEX (user_id, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) Per-user, ephemeral routes
CREATE TABLE IF NOT EXISTS user_routes (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id       BIGINT NOT NULL,
  source_id     BIGINT NULL,
  external_id   VARCHAR(128) NULL,
  name          VARCHAR(300) NULL,
  start_point   POINT SRID 4326 NULL,
  end_point     POINT SRID 4326 NULL,
  path          LINESTRING SRID 4326 NULL,
  distance_m    DOUBLE NULL,
  duration_s    INT NULL,
  details_json  JSON NULL,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at    TIMESTAMP NOT NULL,
  FOREIGN KEY (source_id) REFERENCES location_sources(id),
  UNIQUE KEY uq_user_route_src_xid (user_id, source_id, external_id),
  -- Needed for composite FKs from daily_plan_items (user_id, route_id)
  UNIQUE KEY uq_ur_user_id_id (user_id, id),
  SPATIAL INDEX spx_path (path),
  SPATIAL INDEX spx_start (start_point),
  SPATIAL INDEX spx_end (end_point),
  INDEX (user_id, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) Plans
CREATE TABLE IF NOT EXISTS total_plans (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id    BIGINT NOT NULL,
  title      VARCHAR(200) NOT NULL,
  start_date DATE NULL,
  end_date   DATE NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX (user_id),
  INDEX (start_date),
  INDEX (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS daily_plans (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  total_plan_id BIGINT NOT NULL,
  user_id       BIGINT NOT NULL,
  day_index     INT NOT NULL,        -- 1..N
  date          DATE NULL,
  FOREIGN KEY (total_plan_id) REFERENCES total_plans(id) ON DELETE CASCADE,
  UNIQUE KEY uq_daily_idx (total_plan_id, day_index),
  -- Needed for composite FK from daily_plan_items (user_id, daily_plan_id)
  UNIQUE KEY uq_dp_user_id_id (user_id, id),
  INDEX (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5) Ordered items per day (locations & routes)
CREATE TABLE IF NOT EXISTS daily_plan_items (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id        BIGINT NOT NULL,
  daily_plan_id  BIGINT NOT NULL,
  item_type      ENUM('location','route') NOT NULL,
  location_id    BIGINT NULL,
  route_id       BIGINT NULL,
  position       INT NOT NULL,       -- 10, 20, 30...
  notes          VARCHAR(500) NULL,

  -- Enforce day ownership (same user)
  CONSTRAINT fk_item_day FOREIGN KEY (user_id, daily_plan_id)
    REFERENCES daily_plans(user_id, id) ON DELETE CASCADE,

  -- Enforce that referenced location/route belongs to same user
  CONSTRAINT fk_item_loc FOREIGN KEY (user_id, location_id)
    REFERENCES user_locations(user_id, id) ON DELETE RESTRICT,
  CONSTRAINT fk_item_route FOREIGN KEY (user_id, route_id)
    REFERENCES user_routes(user_id, id) ON DELETE RESTRICT,

  CONSTRAINT chk_one_ref CHECK (
    (item_type='location' AND location_id IS NOT NULL AND route_id IS NULL) OR
    (item_type='route'    AND route_id    IS NOT NULL AND location_id IS NULL)
  ),

  UNIQUE KEY uq_item_order (daily_plan_id, position),
  INDEX (user_id, daily_plan_id, item_type),
  INDEX (location_id),
  INDEX (route_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;