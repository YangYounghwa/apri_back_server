package apri.back_demo.controller;

import apri.back_demo.model.LoginResponse;
import apri.back_demo.model.UserSession;
import apri.back_demo.service.OAuthKakaoService;
import apri.back_demo.service.SessionService;
import apri.back_demo.exception.KakaoResponseException;
import apri.back_demo.model.CheckUserDTO;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;


@RestController  // this automatically adds @ResponseBody to all methods which transforms all return values into JSON
public class LoginController {

    private final CrudController crud;

    public OAuthKakaoService oauthvertify;

    private SessionService sessionService;
    @Value("${com.apri.testingON:false}")
    String testString;

    //Spring calls this automatically
    public LoginController(CrudController crud,OAuthKakaoService oauthvertify, SessionService sessionService){
        this.crud = crud;
        this.oauthvertify=oauthvertify;
        this.sessionService = sessionService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String,Object> reqBody,HttpServletRequest request){

        Long userId=null;
        CheckUserDTO CUD;
        try {
            String token = (String) reqBody.get("ACCESS_TOKEN");
            if(testString.equals("true") || testString.equals("True")) System.err.println("reqBody : " + reqBody.toString());
            CUD = oauthvertify.checkUser(token);
            userId = CUD.getKakao_id();
        } catch (KakaoResponseException e){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("KakaoLoginFail");
        }
        if (userId == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("KakaoLoginFail");
        }
        //create session
        UserSession session = sessionService.createSession(userId);
        LoginResponse response = new LoginResponse("ok", userId, session.getSessionId());
        response.setNewRegister(CUD.isNewRegister()); 
        return ResponseEntity.status(HttpStatus.OK).body(response);
       // every further api call should have
       // Authorization: Session abc123-session-id
       // Authorization: Session ${sessionId}
       // In their headers 
    
    }

    @DeleteMapping("/auth/logout")
public ResponseEntity<?> logout(@RequestHeader Map<String,Object> authHeader) {
    String authString = (String) authHeader.get("sessionId");
    sessionService.deleteSession(authString);
    return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
}


}
