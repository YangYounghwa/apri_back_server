package apri.back_demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import apri.back_demo.dto.request.ApiRequest;
import apri.back_demo.dto.request.AuthDto;
import apri.back_demo.exception.TourAPIVKError;
import apri.back_demo.model.UserSession;
import apri.back_demo.service.SessionService;
import apri.back_demo.service.TourApiCallService;
import apri.back_demo.util.APIResultHandler;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/search")
public class SearchRESTController {

    CrudController crud;
    SessionService sessionService;
    TourApiCallService tapi;

    public SearchRESTController(CrudController crud, SessionService sessionService,TourApiCallService tapi){
        this.crud = crud;
        this.sessionService = sessionService;
        this.tapi = tapi;
    }


    // TODO : consult to frontend, what kind of return type needed
    @PostMapping("/keyword")
    public ResponseEntity<?> searchKeyword(@RequestHeader Map<String,Object> header,@RequestBody ApiRequest<Map<String, Object>> req,HttpServletRequest request){

        Map<String,Object> reqData = req.getData();
        // Session Validation process
        // Automatic login fail exception handled by GloberExceptionHandler 
        AuthDto auth = req.getAuth();
        String authString = (String) auth.getSessionId();
        UserSession userSession = sessionService.validateSession(authString);
        Long apriId = userSession.getApri_id();


        APIResultHandler handler = new APIResultHandler();
        String searchWord = (String) reqData.get("SearchKeyword");
        String cat1 = (String) reqData.get("cat1");
        String cat2 = (String) reqData.get("cat2");
        String cat3 = (String) reqData.get("cat3");


        // TODO : Adjust areacode, sigunguCode to Incheon.
        String msg = null;
        // try-catch 
        msg = this.tapi.apiSearchKeyword(searchWord,cat1,cat2,cat3,null,null,null,null);
        
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

    @PostMapping("/location")
    public ResponseEntity<?> searchLocation(@RequestHeader Map<String,Object> header, @RequestBody ApiRequest<Map<String, Object>> req, HttpServletRequest request){


        APIResultHandler handler = new APIResultHandler();


        Map<String,Object> reqData = req.getData();
        // Session Validation process
        // Automatic login fail exception handled by GloberExceptionHandler 
        AuthDto auth = req.getAuth();
        String authString = (String) auth.getSessionId();
        UserSession userSession = sessionService.validateSession(authString);
        Long ApriId = userSession.getApri_id();


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



    





}
