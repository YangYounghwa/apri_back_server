package apri.back_demo.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class DailyPlanDto {
    private Long id;
    private int dayIndex;
    private LocalDate date;
    private List<PlanItemDto> items;
}
