package apri.back_demo.util;

import graph_routing_01.Finder.model.ApriPath;
import graph_routing_01.Finder.model.ApriPathDTO;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts between your library's ApriPathDTO and DB-friendly WKT,
 * and reconstructs ApriPathDTO from WKT without changing ApriPathDTO's constructor.
 */
public final class GeoDtoConverter {
    private static final GeometryFactory GF = new GeometryFactory();

    private GeoDtoConverter() {}

    /* =========================
       DTO -> WKT
     ========================= */

    /** Flatten segments and produce a single LINESTRING WKT (EPSG:4326 lon/lat). */
    public static String toLineStringWkt(ApriPathDTO dto) {
        List<double[]> flat = flatten(dto);
        if (flat.size() < 2) throw new IllegalArgumentException("LINESTRING requires >= 2 points");
        StringBuilder sb = new StringBuilder("LINESTRING(");
        for (int i = 0; i < flat.size(); i++) {
            if (i > 0) sb.append(", ");
            double[] p = flat.get(i);
            sb.append(p[0]).append(" ").append(p[1]);
        }
        return sb.append(")").toString();
    }

    /** Preserve segments and produce a MULTILINESTRING WKT. */
    public static String toMultiLineStringWkt(ApriPathDTO dto) {
        var segs = requireLineSeg(dto);
        StringBuilder sb = new StringBuilder("MULTILINESTRING(");
        for (int s = 0; s < segs.size(); s++) {
            if (s > 0) sb.append(", ");
            sb.append("(");
            var seg = segs.get(s);
            for (int i = 0; i < seg.size(); i++) {
                if (i > 0) sb.append(", ");
                var pt = seg.get(i);
                sb.append(pt.get(0)).append(" ").append(pt.get(1));
            }
            sb.append(")");
        }
        return sb.append(")").toString();
    }

    /** First [lon,lat] pair. */
    public static double[] startLonLat(ApriPathDTO dto) {
        var a = requireLineSeg(dto).get(0).get(0);
        return new double[]{a.get(0), a.get(1)};
    }

    /** Last [lon,lat] pair. */
    public static double[] endLonLat(ApriPathDTO dto) {
        var segs = requireLineSeg(dto);
        var lastSeg = segs.get(segs.size() - 1);
        var b = lastSeg.get(lastSeg.size() - 1);
        return new double[]{b.get(0), b.get(1)};
    }

    /* =========================
       WKT/Geometry -> DTO (via ApriPath)
     ========================= */

    /**
     * Reconstruct an ApriPathDTO by building an ApriPath (your library type)
     * and delegating to the existing ApriPathDTO(ApriPath) constructor.
     *
     * @param wkt LINESTRING or MULTILINESTRING in EPSG:4326
     * @param totalTime total time fed into ApriPath constructor
     * @param roadName per-segment names (nullable)
     * @param roadType per-segment types (nullable)
     */
    public static ApriPathDTO fromWkt(String wkt, double totalTime,
                                      List<String> roadName, List<String> roadType) {
        try {
            Geometry g = new WKTReader(GF).read(wkt);
            return fromGeometry(g, totalTime, roadName, roadType);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid WKT: " + e.getMessage(), e);
        }
    }

    public static ApriPathDTO fromGeometry(Geometry g, double totalTime,
                                           List<String> roadName, List<String> roadType) {
        ApriPath path = new ApriPath(totalTime);

        if (g instanceof LineString ls) {
            path.addEdge(ls, safeGet(roadName, 0), safeGet(roadType, 0));
        } else if (g instanceof MultiLineString mls) {
            for (int i = 0; i < mls.getNumGeometries(); i++) {
                LineString ls = (LineString) mls.getGeometryN(i);
                path.addEdge(ls, safeGet(roadName, i), safeGet(roadType, i));
            }
        } else {
            throw new IllegalArgumentException("Unsupported geometry type: " + g.getGeometryType());
        }

        return new ApriPathDTO(path);
    }

    /* =========================
       Internals
     ========================= */

    private static List<List<List<Double>>> requireLineSeg(ApriPathDTO dto) {
        try {
            var segs = dto.getLineSeg(); // your DTO must expose a getter
            if (segs == null || segs.isEmpty())
                throw new IllegalArgumentException("lineSeg is null/empty");
            return segs;
        } catch (NoSuchMethodError | NullPointerException e) {
            throw new IllegalStateException("ApriPathDTO must provide getLineSeg()", e);
        }
    }

    private static List<double[]> flatten(ApriPathDTO dto) {
        var segs = requireLineSeg(dto);
        List<double[]> out = new ArrayList<>();
        for (var seg : segs) {
            for (var pt : seg) {
                if (pt == null || pt.size() != 2)
                    throw new IllegalArgumentException("Each point must be [lon,lat]");
                out.add(new double[]{pt.get(0), pt.get(1)});
            }
        }
        return out;
    }

    private static String safeGet(List<String> list, int i) {
        return (list != null && i < list.size()) ? list.get(i) : null;
    }
}