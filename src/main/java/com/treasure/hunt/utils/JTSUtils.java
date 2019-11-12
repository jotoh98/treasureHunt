package com.treasure.hunt.utils;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

public class JTSUtils {
    static GeometryFactory defaultGeometryFactory;
    public static GeometryFactory getDefaultGeometryFactory(){
        if(defaultGeometryFactory==null)
            defaultGeometryFactory = new GeometryFactory();
        return defaultGeometryFactory;
    }
    public static Point createPoint(double x, double y){
        return getDefaultGeometryFactory().createPoint(new Coordinate(x,y));
    }

    public static LineString createLineString(Point A, Point B){
        Coordinate[] coords = {A.getCoordinate(), B.getCoordinate()};
        return getDefaultGeometryFactory().createLineString(coords);
    }

    /**
     * Tests whether the LineString defined by P1 and P2 intersects with the LineString defined by Q1 and Q2.
     * If they do, the intersecting Points are returned.
     * @param P1
     * @param P2
     * @param Q1
     * @param Q2
     * @return
     */
    public static Point[] linesegmentIntersection(Point P1, Point P2, Point Q1, Point Q2){
        Coordinate[] coord1 = {P1.getCoordinate(), P2.getCoordinate()};
        LineString line1 = getDefaultGeometryFactory().createLineString(coord1);
        Coordinate[] coord2 = {Q1.getCoordinate(), Q2.getCoordinate()};
        LineString line2 = getDefaultGeometryFactory().createLineString(coord2);
        Coordinate[] intersect_coords = (line1.intersection(line2)).getCoordinates();
        Point[] ret = new Point[intersect_coords.length];
        for(int i=0; i<intersect_coords.length; i++){
            ret[i] = createPoint(intersect_coords[i].getX(),intersect_coords[i].getY());
        }
        return ret;
    }

    /**
     * Tests whether the line defined by P1 and P2 intersects with the line defined by Q1 and Q2.
     * If they do, the intersecting points are returned.
     * @param P1
     * @param P2
     * @param Q1
     * @param Q2
     * @return
     */
    public static Point[] lineIntersection(Point P1, Point P2, Point Q1, Point Q2){
        // Line P represented as a1x + b1y = c1
        double a1 = P2.getY() - P1.getY(); //B.y - A.y;
        double b1 = P1.getX() - P2.getX(); //A.x - B.x;
        double c1 = a1*P1.getX() + b1*P1.getY(); //a1*(A.x) + b1*(A.y);

        // Line Q represented as a2x + b2y = c2
        double a2 = Q2.getY()-Q1.getY(); //D.y - C.y;
        double b2 = Q1.getX() - Q2.getX(); //C.x - D.x;
        double c2 = a2*Q1.getX() + b2*Q1.getY(); //a2*(C.x)+ b2*(C.y);

        double determinant = a1*b2 - a2*b1;

        if (determinant == 0)
        {
            if(a2/c2 == a1/c1){
                return new Point[]{P1, P2, Q1, Q2}; // the lines are equal
            }
            return new Point[0];
        }
        double x = (b2*c1 - b1*c2)/determinant;
        double y = (a1*c2 - a2*c1)/determinant;
        return new Point[]{createPoint(x, y)};
    }

    /**
     * Tests whether the line defined by L1 and L2 intersects with the linesegment defined by S1 and S2
     * @param L1
     * @param L2
     * @param S1
     * @param S2
     * @return
     */
    public static Point[] lineLinesegmentIntersection(Point L1,Point L2,Point S1,Point S2){
        if(L1==L2 || S1==S2)
            throw new IllegalArgumentException("L1==L2 or S1==S2");
        Point[] intersection = lineIntersection(L1, L2, S1, S2);
        if(intersection.length>1)
            return new Point[]{S1,S2}; // S lies on L

        if(intersection.length==0)
            return intersection;

        if((intersection[0].intersection(createLineString(S1,S2))).isEmpty())
            return new Point[0];

        return intersection;
    }
}
