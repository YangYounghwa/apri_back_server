package apri.back_demo.dto;

import java.util.List;

import lombok.Data;

@Data
public class RouteDto {
   private Long id;
   private String name;
   private PointDto startPoint;
   private PointDto endPoint;
    private List<PointDto> path;
    private Double distanceM; // Meters
    private Double durationS; // Seconds
}
