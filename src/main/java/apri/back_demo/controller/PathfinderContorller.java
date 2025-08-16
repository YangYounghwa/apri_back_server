package apri.back_demo.controller;

import java.time.LocalTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestBody Map<String,Object> reqBody,
            HttpServletRequest request) {

        String authString = (String) header.get("sessionid");
        UserSession userSession = sessionService.validateSession(authString);
        Long userId = userSession.getUserId();


        double stLon = Double.valueOf( (String) reqBody.get("stLon"));
        double stLat = Double.valueOf( (String) reqBody.get("stLat"));
        double endLon = Double.valueOf( (String) reqBody.get("endLon"));
        double endLat = Double.valueOf( (String) reqBody.get("endLat"));
        boolean checkTime = Boolean.valueOf( (String) reqBody.get("checkTime"));
        LocalTime startTime = LocalTime.parse((String) reqBody.get("startTime"));
        int dayNum = Integer.valueOf( (String) reqBody.get("dayNum"));

        ApriPathDTO path = pfs.findPath(stLon,stLat,endLon,endLat,0.0,true,dayNum);

        return ResponseEntity.ok(path);

    }

}
