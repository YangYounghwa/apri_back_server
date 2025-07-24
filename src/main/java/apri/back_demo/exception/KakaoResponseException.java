package apri.back_demo.exception;

public class KakaoResponseException extends RuntimeException{
    public KakaoResponseException(String message){
        super(message);
    }
}
