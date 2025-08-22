package apri.back_demo.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    String ACCESS_TOKEN;
    String REFRESH_TOKEN;
}
