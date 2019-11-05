package com.treasure.hunt.utils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class JTSUtils {
    static GeometryFactory defaultGeometryFactory;
    public static GeometryFactory getDefaultGeometryFactory(){
        if(defaultGeometryFactory==null)
            defaultGeometryFactory = new GeometryFactory();
        return defaultGeometryFactory;
    }
    public static Point givePoint(double x, double y){
        return getDefaultGeometryFactory().createPoint(new Coordinate(x,y));
    }
}
