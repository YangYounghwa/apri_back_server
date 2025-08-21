package apri.back_demo.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanItemDto {
    private Long id;
    private String itemType;
    private int position;
    private String notes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private LocationDto location;
    private RouteDto route;
}
