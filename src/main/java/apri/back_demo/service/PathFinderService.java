package apri.back_demo.service;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import graph_routing_01.Finder.ApriPathFinder;
import graph_routing_01.Finder.exceptions.ApriException;
import graph_routing_01.Finder.exceptions.ApriPathExLibError;
import graph_routing_01.Finder.exceptions.ResException;
import graph_routing_01.Finder.model.ApriPath;
import graph_routing_01.Finder.model.ApriPathDTO;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PathFinderService {

    ApriPathFinder apf = new ApriPathFinder();

    @PostConstruct
    public void init(){

    //     if(false){
    //     Path path = null;
    //     try {
    //         path = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
    //     } catch (URISyntaxException e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //     }
    //     Path baseDir = path.getParent();

    //     File dataFile = new File(baseDir.toFile(), "data/something.csv");
       
    // }
        try {


            File shpFile = Paths.get("data_folder", "simplified_split2.shp").toFile();
            
            System.out.println("Reading split shp.");
            apf.buildBaseGraph(apf.readSHP(shpFile));

            System.out.println("Reading busstop location csv");
        // 정류소의 위치를 도로에 추가해줍니다. 

            File busCsv = Paths.get("data_folder", "busstop_location.csv").toFile();
            apf.addBusstopNodes(busCsv);
        // problem occured here

            File busTimetableCsv = Paths.get("data_folder", "bus_timetable.csv").toFile();
            apf.initBusTimeTable(busTimetableCsv);


            File routeIntervalFile = Paths.get("data_folder", "route_interval_distance.csv").toFile();
            apf.addBusRouteEdges(routeIntervalFile);

                

        } catch (ApriException | ResException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ApriPathExLibError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }




        System.out.println("Service initialized");
    }


    public ApriPathDTO findPath(double stLon, double stLat, double endLon, double endLat)
    {

        return this.findPath(stLon,stLat,endLon,endLat,0.0,true,0);
    }


    public ApriPathDTO findPath(double stLon, double stLat, double endLon, double endLat,double startTime,
    Boolean checkTime, int dayNum){
        ApriPath path = null;
        try {
            path = this.apf.findPath(stLon,stLat,endLon,endLat,startTime,checkTime,dayNum);
        } catch (ApriException | ApriPathExLibError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return new ApriPathDTO(path);
    }
    

    

}
