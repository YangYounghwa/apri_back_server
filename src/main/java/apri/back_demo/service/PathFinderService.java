package apri.back_demo.service;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import graph_routing_01.Finder.ApriPathFinder;
import graph_routing_01.Finder.exceptions.ApriException;
import graph_routing_01.Finder.exceptions.ApriPathExLibError;
import graph_routing_01.Finder.exceptions.ResException;

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
            apf.buildBaseGraph(apf.readSHP("data_folder\\simplified_split2.shp"));


        // 정류소의 위치를 도로에 추가해줍니다. 
            apf.addBusstopNodes("data_folder\\busstop_location.csv");
        // problem occured here

            apf.initBusTimeTable(new File("data_folder\\bus_timetable.csv"));


            apf.addBusRouteEdges("data_folder\\route_interval_distance.csv");

                

        } catch (ApriException | ResException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ApriPathExLibError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }




        System.out.println("Service initialized");
    }



}
