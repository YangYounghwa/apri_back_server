package apri.back_demo.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class TotalPlanDto {
    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
    private List<DailyPlanDto> dailyPlans;
}
