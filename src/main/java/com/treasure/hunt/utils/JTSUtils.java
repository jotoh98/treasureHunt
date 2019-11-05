package com.treasure.hunt.utils;
import org.locationtech.jts.geom.GeometryFactory;

public class JTSUtils {
    static GeometryFactory defaultGeometryFactory;
    public static GeometryFactory getDefaultGeometryFactory(){
        if(defaultGeometryFactory==null)
            defaultGeometryFactory = new GeometryFactory();
        return defaultGeometryFactory;
    }
}
