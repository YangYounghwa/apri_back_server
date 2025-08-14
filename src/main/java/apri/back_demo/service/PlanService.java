package apri.back_demo.service;


import com.fasterxml.jackson.databind.ObjectMapper;

import apri.back_demo.model.plans.LocationDTO;
import apri.back_demo.model.plans.RouteDTO;
import apri.back_demo.util.GeoDtoConverter;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;



import graph_routing_01.Finder.model.ApriPathDTO;

/**
 * JDBC-based service that:
 *  - Upserts per-user locations with TTL
 *  - Inserts routes from your existing ApriPathDTO (no library changes)
 *  - Reads routes back as ApriPathDTO via GeoDtoConverter
 *  - Purges expired user_locations/user_routes + dangling items
 *
 * Assumes the ephemeral schema we discussed earlier (user_locations, user_routes, etc.).
 * Make sure @EnableScheduling is present in your Spring Boot app.
 */
@Service
public class PlanService {

    private final JdbcTemplate jdbc;
    private final ObjectMapper om;

    public PlanService(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.om = objectMapper;
    }

    /* =========================
       Sources
     ========================= */

    @Transactional
    public Long ensureSource(String sourceName) {
        if (sourceName == null || sourceName.isBlank()) return null;
        jdbc.update("""
            INSERT INTO location_sources(name) VALUES (?)
            ON DUPLICATE KEY UPDATE name=VALUES(name)
        """, sourceName);
        return jdbc.queryForObject("SELECT id FROM location_sources WHERE name=?", Long.class, sourceName);
    }

    /* =========================
       Locations (TTL)
     ========================= */

    @Transactional
    public long upsertUserLocation(LocationDTO dto) {
        dto.validate();
        Long sourceId = ensureSource(dto.getSourceName());

        boolean hasPoint = dto.getLon() != null && dto.getLat() != null;
        String pointExpr = hasPoint ? "ST_GeomFromText(?,4326)" : "NULL";
        String detailsJson = toJsonOrNull(dto.getDetails());
        int ttlDays = dto.getTtlDays() != null ? dto.getTtlDays() : 7;

        jdbc.update(con -> {
            String sql = """
                INSERT INTO user_locations
                  (user_id, source_id, external_id, name, address, point, details_json, expires_at)
                VALUES (?, ?, ?, ?, ?, %s, ?, NOW() + INTERVAL ? DAY)
                ON DUPLICATE KEY UPDATE
                  name=VALUES(name),
                  address=VALUES(address),
                  point=VALUES(point),
                  details_json=VALUES(details_json),
                  expires_at=GREATEST(expires_at, VALUES(expires_at))
            """.formatted(pointExpr);
            PreparedStatement ps = con.prepareStatement(sql);
            int i = 1;
            ps.setLong(i++, dto.getUserId());
            if (sourceId == null) ps.setNull(i++, Types.BIGINT); else ps.setLong(i++, sourceId);
            if (dto.getExternalId() == null) ps.setNull(i++, Types.VARCHAR); else ps.setString(i++, dto.getExternalId());
            if (dto.getName() == null) ps.setNull(i++, Types.VARCHAR); else ps.setString(i++, dto.getName());
            if (dto.getAddress() == null) ps.setNull(i++, Types.VARCHAR); else ps.setString(i++, dto.getAddress());
            if (hasPoint) ps.setString(i++, "POINT(" + dto.getLon() + " " + dto.getLat() + ")");
            if (detailsJson == null) ps.setNull(i++, Types.LONGVARCHAR); else ps.setString(i++, detailsJson);
            ps.setInt(i, ttlDays);
            return ps;
        });

        Long id = jdbc.queryForObject("""
            SELECT id FROM user_locations
             WHERE user_id=?
               AND ((? IS NULL AND source_id IS NULL) OR source_id=?)
               AND ((? IS NULL AND external_id IS NULL) OR external_id=?)
        """, Long.class, dto.getUserId(), sourceId, sourceId, dto.getExternalId(), dto.getExternalId());

        if (id == null) throw new IllegalStateException("Upsert user_location failed");
        return id;
    }

    /* =========================
       Routes (ApriPathDTO -> DB)
     ========================= */

    @Transactional
    public long insertUserRoute(RouteDTO dto) {
        dto.validate();

        // Convert ApriPathDTO to WKT + derive start/end
        String wktPath = GeoDtoConverter.toLineStringWkt(dto.getPath());
        double[] a = GeoDtoConverter.startLonLat(dto.getPath());
        double[] b = GeoDtoConverter.endLonLat(dto.getPath());

        String detailsJson = toJsonOrNull(dto.getDetails());
        int ttlDays = dto.getTtlDays() != null ? dto.getTtlDays() : 7;

        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                INSERT INTO user_routes
                  (user_id, name, start_point, end_point, path, distance_m, duration_s, details_json, expires_at)
                VALUES (?, ?, ST_GeomFromText(?,4326), ST_GeomFromText(?,4326),
                        ST_GeomFromText(?,4326), ?, ?, ?, NOW() + INTERVAL ? DAY)
            """, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            ps.setLong(i++, dto.getUserId());
            ps.setString(i++, dto.getName());
            ps.setString(i++, "POINT(" + a[0] + " " + a[1] + ")");
            ps.setString(i++, "POINT(" + b[0] + " " + b[1] + ")");
            ps.setString(i++, wktPath);
            if (dto.getDistanceMeters() == null) ps.setNull(i++, Types.DOUBLE); else ps.setDouble(i++, dto.getDistanceMeters());
            if (dto.getDurationSeconds() == null) ps.setNull(i++, Types.INTEGER); else ps.setInt(i++, dto.getDurationSeconds());
            if (detailsJson == null) ps.setNull(i++, Types.LONGVARCHAR); else ps.setString(i++, detailsJson);
            ps.setInt(i, ttlDays);
            return ps;
        }, kh);

        var key = kh.getKey();
        if (key == null) throw new IllegalStateException("Insert user_route failed");
        return key.longValue();
    }

    /* =========================
       Routes (DB -> ApriPathDTO without changing your lib)
     ========================= */

    @Transactional(readOnly = true)
    public ApriPathDTO getRouteAsApriPathDTO(long userId, long routeId) {
        Map<String, Object> row = jdbc.queryForMap("""
            SELECT ST_AsText(path) AS wkt, COALESCE(duration_s,0) AS duration_s
            FROM user_routes
            WHERE user_id=? AND id=?
        """, userId, routeId);

        String wkt = (String) row.get("wkt");
        int durationSeconds = ((Number) row.get("duration_s")).intValue();

        // Reconstruct DTO using your existing ApriPathDTO(ApriPath) constructor
        return GeoDtoConverter.fromWkt(
                wkt,
                durationSeconds,   // feeding totalTime
                null,              // optional roadName per segment
                null               // optional roadType per segment
        );
    }

    /* =========================
       Scheduled purge of expired rows
     ========================= */

    @Transactional
    @Scheduled(cron = "0 30 3 * * *", zone = "Asia/Seoul")
    public void purgeExpired() {
        // Remove items referencing expired locations
        jdbc.update("""
            DELETE i FROM daily_plan_items i
            JOIN user_locations l ON i.user_id=l.user_id AND i.location_id=l.id
            WHERE l.expires_at < NOW()
        """);
        jdbc.update("DELETE FROM user_locations WHERE expires_at < NOW()");

        // Remove items referencing expired routes
        jdbc.update("""
            DELETE i FROM daily_plan_items i
            JOIN user_routes r ON i.user_id=r.user_id AND i.route_id=r.id
            WHERE r.expires_at < NOW()
        """);
        jdbc.update("DELETE FROM user_routes WHERE expires_at < NOW()");
    }

    /* =========================
       Utils
     ========================= */

    private String toJsonOrNull(Object value) {
        if (value == null) return null;
        try {
            if (value instanceof String s) return s;
            return om.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON payload", e);
        }
    }
}