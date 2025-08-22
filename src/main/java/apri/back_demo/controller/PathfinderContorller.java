package apri.back_demo.controller;

import java.time.LocalTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import apri.back_demo.dto.request.ApiRequest;
import apri.back_demo.dto.request.AuthDto;
import apri.back_demo.model.UserSession;
import apri.back_demo.service.PathFinderService;
import apri.back_demo.service.SessionService;
import graph_routing_01.Finder.model.ApriPathDTO;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class PathfinderContorller {

    @Autowired
    private PathFinderService pfs;

    @Autowired
    SessionService sessionService;

    @PostMapping("/findPath")
    public ResponseEntity<?> postTest(@RequestHeader Map<String, Object> header,
            @RequestBody ApiRequest<Map<String, Object>> req,
            HttpServletRequest request) {

        Map<String,Object> reqData = req.getData();
        // Session Validation process
        // Automatic login fail exception handled by GloberExceptionHandler 
        AuthDto auth = req.getAuth();
        String authString = (String) auth.getSessionId();
        UserSession userSession = sessionService.validateSession(authString);
        Long ApriId = userSession.getApri_id();

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
