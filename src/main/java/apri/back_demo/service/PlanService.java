package apri.back_demo.service;

import apri.back_demo.dto.*;
import graph_routing_01.Finder.model.ApriPathDTO;
import lombok.Data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlanService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * Creates a new total plan and its associated daily plans.
     * @param planDto DTO containing title, dates, and notes.
     * @param apriId The ID of the user creating the plan.
     * @return The created TotalPlanDto with generated IDs.
     */
    @Transactional
    public TotalPlanDto createTotalPlan(TotalPlanDto planDto, Long apriId) {
        // Updated to include 'notes' and remove 'use_yn' (uses DEFAULT 'y')
        String totalPlanSql = "INSERT INTO total_plans (apri_id, title, start_date, end_date, notes) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(totalPlanSql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, apriId);
            ps.setString(2, planDto.getTitle());
            ps.setObject(3, planDto.getStartDate());
            ps.setObject(4, planDto.getEndDate());
            ps.setString(5, planDto.getNotes()); // Set new notes field
            return ps;
        }, keyHolder);

        long totalPlanId = keyHolder.getKey().longValue();
        planDto.setId(totalPlanId);

        List<DailyPlanDto> createdDailyPlans = new ArrayList<>();
        LocalDate currentDate = planDto.getStartDate();
        int dayIndex = 1;
        while (!currentDate.isAfter(planDto.getEndDate())) {
            DailyPlanDto dailyPlan = createDailyPlan(totalPlanId, apriId, dayIndex, currentDate);
            createdDailyPlans.add(dailyPlan);
            currentDate = currentDate.plusDays(1);
            dayIndex++;
        }
        planDto.setDailyPlans(createdDailyPlans);

        return planDto;
    }

    private DailyPlanDto createDailyPlan(long totalPlanId, long apriId, int dayIndex, LocalDate date) {
        String dailyPlanSql = "INSERT INTO daily_plans (total_plan_id, apri_id, day_index, date) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(dailyPlanSql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, totalPlanId);
            ps.setLong(2, apriId);
            ps.setInt(3, dayIndex);
            ps.setObject(4, date);
            return ps;
        }, keyHolder);

        DailyPlanDto dailyPlanDto = new DailyPlanDto();
        dailyPlanDto.setId(keyHolder.getKey().longValue());
        dailyPlanDto.setDayIndex(dayIndex);
        dailyPlanDto.setDate(date);
        dailyPlanDto.setItems(new ArrayList<>());
        return dailyPlanDto;
    }

    public TotalPlanDto getTotalPlanSimple(Long totalPlanId, Long apriId) {
        return getPlan(totalPlanId, apriId, false);
    }

    public TotalPlanDto getTotalPlanFull(Long totalPlanId, Long apriId) {
        return getPlan(totalPlanId, apriId, true);
    }

    /**
     * Fetches a list of all plans for a given user (summary view).
     * @param apriId The ID of the user.
     * @return A list of TotalPlanSummaryDto.
     */
    public List<TotalPlanSummaryDto> getAllPlansForUser(Long apriId) {
        // Updated to select the new 'notes' field
        String sql = "SELECT id, title, start_date, end_date, notes FROM total_plans WHERE apri_id = ? AND use_yn = 'y' ORDER BY start_date DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TotalPlanSummaryDto summary = new TotalPlanSummaryDto();
            summary.setId(rs.getLong("id"));
            summary.setTitle(rs.getString("title"));
            summary.setStartDate(rs.getObject("start_date", LocalDate.class));
            summary.setEndDate(rs.getObject("end_date", LocalDate.class));
            summary.setNotes(rs.getString("notes")); // Set new notes field
            return summary;
        }, apriId);
    }

    private TotalPlanDto getPlan(Long totalPlanId, Long apriId, boolean includeFullRoute) {
        // 1. Fetch Total Plan info - Updated to select 'notes'
        String totalPlanSql = "SELECT id, title, start_date, end_date, notes FROM total_plans WHERE id = ? AND apri_id = ?";
        TotalPlanDto totalPlan = jdbcTemplate.queryForObject(totalPlanSql, (rs, rowNum) -> {
            TotalPlanDto dto = new TotalPlanDto();
            dto.setId(rs.getLong("id"));
            dto.setTitle(rs.getString("title"));
            dto.setStartDate(rs.getObject("start_date", LocalDate.class));
            dto.setEndDate(rs.getObject("end_date", LocalDate.class));
            dto.setNotes(rs.getString("notes")); // Set new notes field
            return dto;
        }, totalPlanId, apriId);

        if (totalPlan == null) return null;

        // 2. Fetch all daily plans and their items (this query remains the same)
        // ... (The rest of this complex method is unchanged as it fetches daily/item details)
        String itemsSql = """
            SELECT
                dp.id AS daily_plan_id, dp.day_index, dp.date,
                dpi.id AS item_id, dpi.item_type, dpi.position, dpi.notes, dpi.start_time, dpi.end_time,
                ul.id AS loc_id, ul.name AS loc_name, ul.address AS loc_address, ST_AsText(ul.point) AS loc_point,
                ur.id AS route_id, ur.name AS route_name, ST_AsText(ur.start_point) AS route_start, ST_AsText(ur.end_point) AS route_end,
                ST_AsText(ur.path) AS route_path, ur.distance_m, ur.duration_s, ur.segments_json
            FROM daily_plans dp
            LEFT JOIN daily_plan_items dpi ON dp.id = dpi.daily_plan_id
            LEFT JOIN user_locations ul ON dpi.location_id = ul.id
            LEFT JOIN user_routes ur ON dpi.route_id = ur.id
            WHERE dp.total_plan_id = ? AND dp.apri_id = ?
            ORDER BY dp.day_index, dpi.position
        """;

        List<PlanItemRow> rows = jdbcTemplate.query(itemsSql, new PlanItemRowMapper(), totalPlanId, apriId);

        Map<Long, DailyPlanDto> dailyPlanMap = rows.stream()
            .collect(Collectors.groupingBy(PlanItemRow::getDailyPlanId))
            .entrySet().stream()
            .map(entry -> {
                PlanItemRow firstRow = entry.getValue().get(0);
                DailyPlanDto dailyPlanDto = new DailyPlanDto();
                dailyPlanDto.setId(firstRow.getDailyPlanId());
                dailyPlanDto.setDayIndex(firstRow.getDayIndex());
                dailyPlanDto.setDate(firstRow.getDate());
                
                List<PlanItemDto> items = entry.getValue().stream()
                    .filter(row -> row.getItemId() != null)
                    .map(row -> mapRowToPlanItemDto(row, includeFullRoute))
                    .collect(Collectors.toList());
                dailyPlanDto.setItems(items);
                
                return dailyPlanDto;
            })
            .collect(Collectors.toMap(DailyPlanDto::getId, dto -> dto));
            
        String allDailyPlansSql = "SELECT id, day_index, date FROM daily_plans WHERE total_plan_id = ?";
        jdbcTemplate.query(allDailyPlansSql, (rs) -> {
            long dailyPlanId = rs.getLong("id");
            if (!dailyPlanMap.containsKey(dailyPlanId)) {
                DailyPlanDto emptyDailyPlan = new DailyPlanDto();
                emptyDailyPlan.setId(dailyPlanId);
                emptyDailyPlan.setDayIndex(rs.getInt("day_index"));
                emptyDailyPlan.setDate(rs.getObject("date", LocalDate.class));
                emptyDailyPlan.setItems(new ArrayList<>());
                dailyPlanMap.put(dailyPlanId, emptyDailyPlan);
            }
        }, totalPlanId);


        totalPlan.setDailyPlans(new ArrayList<>(dailyPlanMap.values()));
        totalPlan.getDailyPlans().sort((d1, d2) -> Integer.compare(d1.getDayIndex(), d2.getDayIndex()));
        
        return totalPlan;
    }
    
    /**
     * [NEW METHOD] Updates the title and notes of a total plan.
     * @param totalPlanId The ID of the plan to update.
     * @param apriId The ID of the user for authorization.
     * @param title The new title.
     * @param notes The new notes.
     * @return true if the update was successful.
     */
    public boolean updateTotalPlanDetails(Long totalPlanId, Long apriId, String title, String notes) {
        String sql = "UPDATE total_plans SET title = ?, notes = ? WHERE id = ? AND apri_id = ?";
        return jdbcTemplate.update(sql, title, notes, totalPlanId, apriId) > 0;
    }

    @Transactional
    public boolean updateDailyPlanItems(Long dailyPlanId, List<PlanItemDto> items, Long apriId) {
        String checkOwnerSql = "SELECT COUNT(*) FROM daily_plans WHERE id = ? AND apri_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkOwnerSql, Integer.class, dailyPlanId, apriId);
        if (count == null || count == 0) {
            return false;
        }

        String deleteSql = "DELETE FROM daily_plan_items WHERE daily_plan_id = ?";
        jdbcTemplate.update(deleteSql, dailyPlanId);

        String insertSql = "INSERT INTO daily_plan_items (apri_id, daily_plan_id, item_type, location_id, route_id, position, notes, start_time, end_time) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        List<Object[]> batchArgs = items.stream()
            .map(item -> new Object[]{
                apriId,
                dailyPlanId,
                item.getItemType(),
                "location".equals(item.getItemType()) ? item.getLocation().getId() : null,
                "route".equals(item.getItemType()) ? item.getRoute().getId() : null,
                item.getPosition(),
                item.getNotes(),
                item.getStartTime(),
                item.getEndTime()
            })
            .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(insertSql, batchArgs);
        return true;
    }
    
    public boolean deleteTotalPlan(Long totalPlanId, Long apriId) {
        String sql = "DELETE FROM total_plans WHERE id = ? AND apri_id = ?";
        return jdbcTemplate.update(sql, totalPlanId, apriId) > 0;
    }

    private PlanItemDto mapRowToPlanItemDto(PlanItemRow row, boolean includeFullRoute) {
        // This method is unchanged
        // ...
        PlanItemDto item = new PlanItemDto();
        item.setId(row.getItemId());
        item.setItemType(row.getItemType());
        item.setPosition(row.getPosition());
        item.setNotes(row.getNotes());
        item.setStartTime(row.getStartTime());
        item.setEndTime(row.getEndTime());

        if ("location".equals(row.getItemType())) {
            LocationDto loc = new LocationDto();
            loc.setId(row.getLocId());
            loc.setName(row.getLocName());
            loc.setAddress(row.getLocAddress());
            loc.setPoint(parsePoint(row.getLocPoint()));
            item.setLocation(loc);
        } else if ("route".equals(row.getItemType())) {
            RouteDto route = includeFullRoute ? new FullRouteDto() : new RouteDto();
            route.setId(row.getRouteId());
            route.setName(row.getRouteName());
            route.setStartPoint(parsePoint(row.getRouteStart()));
            route.setEndPoint(parsePoint(row.getRouteEnd()));
            route.setPath(parseLineString(row.getRoutePath()));
            route.setDistanceM(row.getDistanceM());
            route.setDurationS(row.getDurationS());

            if (includeFullRoute && row.getSegmentsJson() != null) {
                try {
                    ApriPathDTO segments = objectMapper.readValue(row.getSegmentsJson(), ApriPathDTO.class);
                    ((FullRouteDto) route).setSegments(segments);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            item.setRoute(route);
        }
        return item;
    }
    
    private PointDto parsePoint(String wkt) {
        if (wkt == null || !wkt.startsWith("POINT")) return null;
        String[] coords = wkt.replace("POINT(", "").replace(")", "").split(" ");
        return new PointDto(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));
    }

    private List<PointDto> parseLineString(String wkt) {
        if (wkt == null || !wkt.startsWith("LINESTRING")) return null;
        String[] points = wkt.replace("LINESTRING(", "").replace(")", "").split(",");
        List<PointDto> path = new ArrayList<>();
        for (String pointStr : points) {
            String[] coords = pointStr.trim().split(" ");
            path.add(new PointDto(Double.parseDouble(coords[1]), Double.parseDouble(coords[0])));
        }
        return path;
    }

    @Data
    private static class PlanItemRow {
        private Long dailyPlanId;
        private int dayIndex;
        private LocalDate date;
        private Long itemId;
        private String itemType;
        private int position;
        private String notes;
        private java.time.LocalDateTime startTime;
        private java.time.LocalDateTime endTime;
        private Long locId;
        private String locName;
        private String locAddress;
        private String locPoint;
        private Long routeId;
        private String routeName;
        private String routeStart;
        private String routeEnd;
        private String routePath;
        private Double distanceM;
        private Double durationS;
        private String segmentsJson;
    }

    private static class PlanItemRowMapper implements RowMapper<PlanItemRow> {
        @Override
        public PlanItemRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            // This mapper is unchanged
            // ...
            PlanItemRow row = new PlanItemRow();
            row.setDailyPlanId(rs.getLong("daily_plan_id"));
            row.setDayIndex(rs.getInt("day_index"));
            row.setDate(rs.getObject("date", LocalDate.class));
            
            row.setItemId(rs.getObject("item_id", Long.class));
            if (row.getItemId() == null) {
                return row;
            }

            row.setItemType(rs.getString("item_type"));
            row.setPosition(rs.getInt("position"));
            row.setNotes(rs.getString("notes"));
            row.setStartTime(rs.getObject("start_time", java.time.LocalDateTime.class));
            row.setEndTime(rs.getObject("end_time", java.time.LocalDateTime.class));
            
            row.setLocId(rs.getObject("loc_id", Long.class));
            row.setLocName(rs.getString("loc_name"));
            row.setLocAddress(rs.getString("loc_address"));
            row.setLocPoint(rs.getString("loc_point"));

            row.setRouteId(rs.getObject("route_id", Long.class));
            row.setRouteName(rs.getString("route_name"));
            row.setRouteStart(rs.getString("route_start"));
            row.setRouteEnd(rs.getString("route_end"));
            row.setRoutePath(rs.getString("route_path"));
            row.setDistanceM(rs.getObject("distance_m", Double.class));
            row.setDurationS(rs.getObject("duration_s", Double.class));
            row.setSegmentsJson(rs.getString("segments_json"));
            
            return row;
        }
    }
}