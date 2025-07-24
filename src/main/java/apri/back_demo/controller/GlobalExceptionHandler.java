package apri.back_demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import apri.back_demo.exception.KakaoResponseException;
import apri.back_demo.exception.NoSessionFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final int KAKAO_RES_ERROR_CODE = 530;
    @ExceptionHandler(NoSessionFoundException.class)
    public ResponseEntity<?> handleNoSession(){
        return ResponseEntity.status(401).body(Map.of(
            "error", "Invalid or expired session",
            "code",401));

    }

    @ExceptionHandler(KakaoResponseException.class)
    public ResponseEntity<?> handleKakaoError(){
        return ResponseEntity.status(KAKAO_RES_ERROR_CODE).body(Map.of(
            "error","Invalid kakao server return value",
            "code",KAKAO_RES_ERROR_CODE
        ));
    }
}
