package apri.back_demo.dto.request.data;

import lombok.Data;

@Data
public class UpdatePlanDetailsData {
    private Long planId;
    private String title;
    private String notes;
}
