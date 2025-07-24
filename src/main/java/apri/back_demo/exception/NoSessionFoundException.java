package apri.back_demo.exception;


/**
 * 
 * Handled by GlobalExceptionHandler,  returns 401 error
 */
public class NoSessionFoundException extends RuntimeException{
    public NoSessionFoundException(String message){
        super(message);
    }
}
