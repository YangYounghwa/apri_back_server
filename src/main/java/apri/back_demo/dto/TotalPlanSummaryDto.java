package apri.back_demo.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class TotalPlanSummaryDto {
    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes; 
}
