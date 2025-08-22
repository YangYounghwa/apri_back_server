package apri.back_demo.controller;

import java.time.LocalTime;
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

import apri.back_demo.dto.request.ApiRequest;
import apri.back_demo.dto.request.AuthDto;
import apri.back_demo.dto.response.LoginResponse;
import apri.back_demo.exception.KakaoResponseException;
import apri.back_demo.exception.NoSessionFoundException;
import apri.back_demo.exception.TourAPIVKError;
import apri.back_demo.model.UserSession;
import apri.back_demo.service.OAuthKakaoService;
import apri.back_demo.service.PathFinderService;
import apri.back_demo.service.SessionService;
import apri.back_demo.service.TourApiCallService;
import apri.back_demo.util.APIResultHandler;
import graph_routing_01.Finder.model.ApriPathDTO;
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


    @Autowired
    private PathFinderService pfs;

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
            @RequestBody ApiRequest<Map<String, Object>> req, HttpServletRequest request) {

        if (!this.testingON)
            return ResponseEntity.noContent().build();


        Map<String,Object> reqData = req.getData();
        // Session Validation process
        // Automatic login fail exception handled by GloberExceptionHandler 
        AuthDto auth = req.getAuth();
        String authString = (String) auth.getSessionId();
        UserSession userSession = sessionService.validateSession(authString);
        Long ApriId = userSession.getApri_id();


        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("sessionId", userSession.getSessionId(), "userId", ApriId));

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
            @RequestBody ApiRequest<Map<String, Object>> req, HttpServletRequest request) {

        if (!this.testingON)
            return ResponseEntity.noContent().build();

        Map<String,Object> reqData = req.getData();
        // Session Validation process
        // Automatic login fail exception handled by GloberExceptionHandler 
        // AuthDto auth = req.getAuth();
        // String authString = (String) auth.getSessionId();
        // UserSession userSession = sessionService.validateSession(authString);
        // Long ApriId = userSession.getApri_id();


        APIResultHandler handler = new APIResultHandler();
        String searchWord = (String) reqData.get("SearchKeyword");
        String cat1 = (String) reqData.get("cat1");
        String cat2 = (String) reqData.get("cat2");
        String cat3 = (String) reqData.get("cat3");

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
            @RequestBody ApiRequest<Map<String, Object>> req, HttpServletRequest request) {
        if (!this.testingON)
            return ResponseEntity.noContent().build();


        Map<String,Object> reqData = req.getData();
        APIResultHandler handler = new APIResultHandler();

        String posXString = (String) reqData.get("posX");
        String posYString = (String) reqData.get("posY");

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
        Long apri_id = (long) 1;
        UserSession session = sessionService.createSession(userId, apri_id);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("sessionid", session.getSessionId()));
    }


    @PostMapping("/test/findPath")
    public ResponseEntity<?> postTest(@RequestHeader Map<String, Object> header,
            @RequestBody ApiRequest<Map<String, Object>> req,
            HttpServletRequest request) {



        Map<String,Object> reqData = req.getData();
        if (!this.testingON)
            return ResponseEntity.noContent().build();

        double stLon = Double.valueOf( (String) reqData.get("stLon"));
        double stLat = Double.valueOf( (String) reqData.get("stLat"));
        double endLon = Double.valueOf( (String) reqData.get("endLon"));
        double endLat = Double.valueOf( (String) reqData.get("endLat"));
        boolean checkTime = Boolean.valueOf( (String) reqData.get("checkTime"));
        LocalTime startTime = LocalTime.parse((String) reqData.get("startTime"));
        int dayNum = Integer.valueOf( (String) reqData.get("dayNum"));

        ApriPathDTO path = pfs.findPath(stLon,stLat,endLon,endLat,0.0,true,dayNum);

        return ResponseEntity.ok(path);

    }



}