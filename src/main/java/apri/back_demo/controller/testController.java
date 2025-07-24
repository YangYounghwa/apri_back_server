package apri.back_demo.controller;

import java.util.List;
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
import apri.back_demo.exception.TourAPIVKError;
import apri.back_demo.model.LoginResponse;
import apri.back_demo.model.UserSession;
import apri.back_demo.service.OAuthKakaoService;
import apri.back_demo.service.SessionService;
import apri.back_demo.service.TourApiCallService;
import apri.back_demo.util.APIResultHandler;
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
    TourApiCallService tapi;

    public testController(SessionService sessionService, OAuthKakaoService oAuthKakaoService,
            TourApiCallService tourApiCallService) {
        this.oauthvertify = oAuthKakaoService;
        this.sessionService = sessionService;
        this.tapi = tourApiCallService;

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

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("sessionId", userSession.getSessionId(), "userId", userId));

    }

    @GetMapping("/test/AuthHGET")
    public ResponseEntity<?> getTestProfile(@RequestHeader Map<String, Object> authHeader,
            HttpServletRequest request) throws NoSessionFoundException {
        if (!this.testingON)
            return ResponseEntity.noContent().build();
        String authString = (String) authHeader.get("sessionId");
        UserSession userSession = sessionService.validateSession(authString);

        Long userId = userSession.getUserId();

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("sessionId", userSession.getSessionId(), "userId", userId));
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

    @PostMapping("/test/tourapi/keyword")
    public ResponseEntity<?> testSearchKeyword(@RequestHeader Map<String, Object> header,
            @RequestBody Map<String, Object> reqBody, HttpServletRequest request) {

        if (!this.testingON)
            return ResponseEntity.noContent().build();

        APIResultHandler handler = new APIResultHandler();
        String searchWord = (String) reqBody.get("SearchKeyword");
        String cat1 = (String) reqBody.get("cat1");
        String cat2 = (String) reqBody.get("cat2");
        String cat3 = (String) reqBody.get("cat3");

        // TODO : Adjust areacode, sigunguCode to Incheon.
        String msg = null;
        // try-catch
        msg = this.tapi.apiSearchKeyword(searchWord, cat1, cat2, cat3, null, null, null, null);

        @SuppressWarnings("unused")
        List<Map<String, Object>> searchResult = null;

        try {
            searchResult = handler.returnAsList(msg);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ResponseEntity.ok(searchResult);
    }

    @PostMapping("/test/tourapi/location")
    public ResponseEntity<?> testSearchLocation(@RequestHeader Map<String, Object> header,
            @RequestBody Map<String, Object> reqBody, HttpServletRequest request) {
        if (!this.testingON)
            return ResponseEntity.noContent().build();
        APIResultHandler handler = new APIResultHandler();

        String posXString = (String) reqBody.get("posX");
        String posYString = (String) reqBody.get("posY");

        double posX = Double.valueOf(posXString);
        double posY = Double.valueOf(posYString);
        String msg;
        msg = this.tapi.apiLocationBasedList(posX, posY, 1000);// radius = 1000 m
        List<Map<String, Object>> searchResult = null;
        try {
            searchResult = handler.returnAsList(msg);
        } catch (TourAPIVKError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ResponseEntity.ok(searchResult);
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