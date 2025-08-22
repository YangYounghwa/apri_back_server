package apri.back_demo.dto.request.data;

import java.time.LocalDate;

import lombok.Data;

@Data
public class CreatePlanData {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes; 
}
