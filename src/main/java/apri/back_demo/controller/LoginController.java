package apri.back_demo.controller;

import apri.back_demo.model.UserSession;
import apri.back_demo.service.OAuthKakaoService;
import apri.back_demo.service.SessionService;
import apri.back_demo.dto.CheckUserDTO;
import apri.back_demo.dto.request.ApiRequest;
import apri.back_demo.dto.request.AuthDto;
import apri.back_demo.dto.request.LoginRequest;
import apri.back_demo.dto.response.LoginResponse;
import apri.back_demo.exception.KakaoResponseException;

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
    public ResponseEntity<?> login(@RequestBody LoginRequest req,HttpServletRequest request){

        Map<String,Object> reqData = (Map<String, Object>) req;
        Long kakao_id=null;
        Long apri_id=null;
        CheckUserDTO CUD;
        try {
            String token = (String) reqData.get("ACCESS_TOKEN");
            if(testString.equals("true") || testString.equals("True")) System.err.println("reqBody : " + reqData.toString());
            CUD = oauthvertify.checkUser(token);
            kakao_id = CUD.getKakao_id();
            apri_id= CUD.getApri_id();
        } catch (KakaoResponseException e){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("KakaoLoginFail");
        }
        if (kakao_id == null)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("KakaoLoginFail");
        }
        //create session
        UserSession session = sessionService.createSession(kakao_id,apri_id);
        LoginResponse response = new LoginResponse("ok", kakao_id, session.getSessionId());
        response.setNewRegister(CUD.isNewRegister()); 
        return ResponseEntity.status(HttpStatus.OK).body(response);
       // every further api call should have
       // Authorization: Session abc123-session-id
       // Authorization: Session ${sessionId}
       // In their headers 
    
    }

    @DeleteMapping("/auth/logout")
public ResponseEntity<?> logout(@RequestBody ApiRequest<Map<String, Object>> req) {

     AuthDto auth = req.getAuth();
        String authString = (String) auth.getSessionId();
        UserSession userSession = sessionService.validateSession(authString);
        Long ApriId = userSession.getApri_id();
    sessionService.deleteSession(authString);
    return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
}


}
