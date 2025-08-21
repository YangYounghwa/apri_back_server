package apri.back_demo.dto;

import graph_routing_01.Finder.model.ApriPathDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FullRouteDto extends RouteDto{
    private ApriPathDTO segments;
    
}
