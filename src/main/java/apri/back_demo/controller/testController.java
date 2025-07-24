package apri.back_demo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import apri.back_demo.exception.KakaoResponseException;
import apri.back_demo.exception.NoSessionFoundException;
import apri.back_demo.model.LoginResponse;
import apri.back_demo.model.UserSession;
import apri.back_demo.service.OAuthKakaoService;
import apri.back_demo.service.SessionService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class testController {
    private SessionService sessionService;

    @Value("${com.apri.testingON:false}")
    String testString;
    private boolean testingON = false;
    // Properties are only available when properties
    //
    public OAuthKakaoService oauthvertify;

    public testController(SessionService sessionService, OAuthKakaoService oAuthKakaoService) {
        this.oauthvertify = oAuthKakaoService;
        this.sessionService = sessionService;

    }

    @PostConstruct
    public void init() {
        if (testString.equals("true") || testString.equals("True"))
            testingON = true;
    }

    @PostMapping("/test/AuthPOST")
    public ResponseEntity<?> postTestProfile(@RequestHeader Map<String, Object> authHeader,
            @RequestBody String testBody, HttpServletRequest request) {

        if (!this.testingON)
            return ResponseEntity.noContent().build();
        String authString = (String) authHeader.get("sessionId");
        UserSession userSession = sessionService.validateSession(authString);
        Long userId = userSession.getUserId();

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("sessionId", userSession.getSessionId(),"userId",userId));

    }

    @GetMapping("/test/AuthHGET")
    public ResponseEntity<?> getTestProfile(@RequestHeader Map<String, Object> authHeader,
            HttpServletRequest request) throws NoSessionFoundException {
        if (!this.testingON)
            return ResponseEntity.noContent().build();
        String authString = (String) authHeader.get("sessionId");
        UserSession userSession = sessionService.validateSession(authString);

        Long userId = userSession.getUserId();

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("sessionId", userSession.getSessionId(),"userId",userId));
    }

    @PostMapping("/test/POST")
    public ResponseEntity<?> postTest(@RequestHeader Map<String, Object> authHeader,
            @RequestBody String testBody,
            HttpServletRequest request) {
        if (!this.testingON)
            return ResponseEntity.noContent().build();

        return ResponseEntity.ok(Map.of("test", "test"));

    }

    @GetMapping("/test/GET")
    public ResponseEntity<?> getTest(@RequestHeader Map<String, Object> authHeader,
            HttpServletRequest request) throws NoSessionFoundException {

        if (!this.testingON)
            return ResponseEntity.noContent().build();

        return ResponseEntity.ok(Map.of("test", "test"));
    }

    @PostMapping("/test/tourapi")
    public ResponseEntity<?> basicTest(@RequestHeader Map<String, Object> authHeader,
            HttpServletRequest request) {
        if (!this.testingON)
            return ResponseEntity.noContent().build();

        return null;
    }

    @PostMapping("/test/sessionCreateTest")
    public ResponseEntity<?> sessionTest(HttpServletRequest request) {

        if (!this.testingON)
            return ResponseEntity.noContent().build();
        Long userId = (long) 1;
        UserSession session = sessionService.createSession(userId);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("sessionId", session.getSessionId()));
    }
}