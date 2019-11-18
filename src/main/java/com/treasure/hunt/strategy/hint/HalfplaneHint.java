package com.treasure.hunt.strategy.hint;

import lombok.Value;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Point;

@Value
public class HalfplaneHint extends Hint {

    public enum Direction{
        right, left, up, down;
    }

    Point halfplanePointOne;
    Point halfplanePointTwo;
    Direction direction;    // when the line indicated by halfplanePointOne and halfplanePointTwo is not horizontal,
                            // right and left indicate where the target is (right indicates the target is in positive x-Direction
                            // in relationship to the line)
                            // when the line is horizontal, up signals the target is in positive y-Direction in relationship
                            // to the line (the up and down enumerators are only used when the line is horizontal)
                            // left and down respectively

    public static HalfplaneHint angular2correctHalfPlaneHint(AngleHint anglehint)
    {


        Point P1 = anglehint.getAnglePointOne();
        Point P2 = anglehint.getAnglePointTwo();
        Point C = anglehint.getAngleCenter();

        double yPointOne = P1.getY();
        double xPointOne = P1.getX();
        double yCenter = C.getY();
        double xCenter = C.getX();
        double yPointTwo = P2.getY();
        double xPointTwo = P2.getX();

        if(Angle.angleBetweenOriented(P1.getCoordinate(), C.getCoordinate(), P2.getCoordinate())<=0){
            throw new IllegalArgumentException("angular2correctHalfPlaneHint was called with an angular Hint bigger"+
                    " than pi or equal to 0.");
        }

        if(yPointOne == yCenter){
            if(xPointOne>xCenter){
                return new HalfplaneHint(P1, C, Direction.up);
            }
            return new HalfplaneHint(P1, C, Direction.down);
        }

        if(yPointOne<yCenter){
            return new HalfplaneHint(P1, C, Direction.right);
        }

        return new HalfplaneHint(P1,C, Direction.left);
    }

}