package apri.back_demo.dto;

import lombok.Data;

@Data
public class LocationDto {
    private Long id;
    private String name;
    private String address;
    private PointDto point; 
}
