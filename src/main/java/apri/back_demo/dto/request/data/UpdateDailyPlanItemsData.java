package apri.back_demo.dto.request.data;

import java.util.List;

import apri.back_demo.dto.PlanItemDto;
import lombok.Data;

@Data
public class UpdateDailyPlanItemsData {
    private Long dailyPlanId;
    private List<PlanItemDto> items; 
}
