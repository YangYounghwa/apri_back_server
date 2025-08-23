package apri.back_demo.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
  // --- ANNOTATIONS ADDED HERE ---
  // This tells the JSON deserializer to automatically detect the correct subclass
  // based on the fields present in the JSON data. If a "segments" field exists,
  // it will correctly create a FullRouteDto object.
  @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
  @JsonSubTypes({
    @JsonSubTypes.Type(FullRouteDto.class),
    @JsonSubTypes.Type(RouteDto.class)
  })
  private RouteDto route;
}
