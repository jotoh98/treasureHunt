package com.treasure.hunt.strategy.searcher.impl.ias;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.operation.distance.DistanceOp;

import java.util.Arrays;
import java.util.Collections;

/*
* Searches the border of the area-polygon by going to the point of the boundary
*  which is FRACTION * perimeter(area) away from the current location in terms of the perimeter of the area.
*
* @author Vincent Schönbach
* */

public class IASStrategyBorderSearch implements IASMovementStrategy {
    private final double FRACTION = 1/3.;


    @Override
    public void run(IntelligentAngleSearcher s) {
        Polygon area = s.area;

        //Get closest point of area boundary from location
        Coordinate startingPoint = DistanceOp.nearestPoints(area.getBoundary(), JTSUtils.GEOMETRY_FACTORY.createPoint(s.location))[0];

        s.gotoPoint(startingPoint);

        // Calculate point on boundary
        LineString border = (LineString)area.getBoundary().getGeometryN(0).copy();
        Coordinate[] coords = border.getCoordinateSequence().copy().toCoordinateArray();

        double startPosDistToFirstCorner = 0;

        for (int i = 0; i < coords.length; i++){
            int k = i != coords.length-1 ? i+1 : 0;
            LineSegment line = new LineSegment(coords[i], coords[k]);
            if (line.distance(startingPoint) < 0.001){
                startPosDistToFirstCorner = startingPoint.distance(coords[k]);
                Collections.rotate(Arrays.asList(coords), -i-1);
                break;
            }
        }

        double distanceToGo = FRACTION * border.getLength() - startPosDistToFirstCorner;

        for (int i = 0; i < coords.length; i++){
            int k = i != coords.length-1 ? i+1 : 0;
            distanceToGo -= coords[i].distance(coords[k]);

            if (distanceToGo < 0)
            {
                Coordinate destination = JTSUtils.coordinateInDistance(coords[k], coords[i], -distanceToGo);
                s.gotoPoint(destination);
                break;
            }
        }
    }
}
