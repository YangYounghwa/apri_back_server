package apri.back_demo.dto.request;

import lombok.Data;

/**
 * A generic wrapper for all API requests
 * @param <T> The type of the specific data payload for the action.
 */
@Data
public class ApiRequest<T> {
    private AuthDto auth;
    private T data; 
}
