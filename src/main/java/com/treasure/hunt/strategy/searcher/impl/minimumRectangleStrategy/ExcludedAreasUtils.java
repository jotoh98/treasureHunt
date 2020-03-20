package com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions to exclude areas which do not need to be looked at (areas already visited and areas which are
 * excluded by hints)
 *
 * @author Rank
 */

public class ExcludedAreasUtils {
    /**
     * Tests if the intersection of hintOne and hintTwo lies in all other half-planes of the hints in otherHintsOne,
     * and otherHintsTwo.
     * If this is the case the intersection gets added to intersectionList, otherwise nothing is done.
     *
     * @param hintOne
     * @param hintTwo
     * @param otherHintsOne
     * @param otherHintsTwo
     */
    static void addIntersectionIfInPoly(HalfPlaneHint hintOne, HalfPlaneHint hintTwo,
                                        List<HalfPlaneHint> otherHintsOne, List<HalfPlaneHint> otherHintsTwo,
                                        ArrayList<Coordinate> newPolygonCorners) {
        Coordinate intersection = hintOne.getHalfPlaneLine().lineIntersection(hintTwo.getHalfPlaneLine());
        if (intersection == null) {
            return;
        }

        for (HalfPlaneHint hint : otherHintsOne) {
            if (hint != hintOne && hint != hintTwo) {
                if (!hint.inHalfPlane(intersection)) {
                    return;
                }
            }
        }

        for (HalfPlaneHint hint : otherHintsTwo) {
            if (hint != hintOne && hint != hintTwo) {
                if (!hint.inHalfPlane(intersection)) {
                    return;
                }
            }
        }

        newPolygonCorners.add(intersection);
    }

    /**
     * When the phase has changed, this method can be called and it returns the new polygon if its not empty.
     * If it is empty, null is returned.
     *
     * @return the new polygon if it isn't empty, null otherwise
     */
    static Polygon intersectHints(List<HalfPlaneHint> hintListOne, List<HalfPlaneHint> hintListTwo) {
        ArrayList<Coordinate> newPolygonCorners;

        newPolygonCorners = new ArrayList<>();

        for (HalfPlaneHint hintOne : hintListOne) {
            for (HalfPlaneHint hintTwo : hintListOne) {
                addIntersectionIfInPoly(hintOne, hintTwo,
                        hintListOne, hintListTwo, newPolygonCorners);
            }
            for (HalfPlaneHint hintTwo : hintListTwo) {
                addIntersectionIfInPoly(hintOne, hintTwo,
                        hintListOne, hintListTwo, newPolygonCorners);
            }
        }
        for (HalfPlaneHint hintOne : hintListTwo) {
            for (HalfPlaneHint hintTwo : hintListTwo) {
                addIntersectionIfInPoly(hintOne, hintTwo,
                        hintListOne, hintListTwo, newPolygonCorners);
            }
        }

        if (newPolygonCorners.size() <= 2)
            return null;

        newPolygonCorners.add(newPolygonCorners.get(0));
        Geometry newPolygon = JTSUtils.GEOMETRY_FACTORY.createPolygon(
                newPolygonCorners.toArray(new Coordinate[]{}));
        newPolygon = newPolygon.convexHull();

        if (newPolygon.getArea() == 0) {
            return null;
        }

        return (Polygon) newPolygon;
    }

    static Geometry visitedPolygon(Point lastLocation, SearchPath move) {
        if (lastLocation == null || move == null) {
            throw new IllegalArgumentException("lastLocation or move is null");
        }
        Coordinate[] movesCoordinates = new Coordinate[move.getPoints().size() + 1];
        movesCoordinates[0] = lastLocation.getCoordinate();
        for (int i = 0; i < move.getPoints().size(); i++) {
            movesCoordinates[i + 1] = move.getPoints().get(i).getCoordinate();
        }
        if (movesCoordinates.length == 1) {
            return JTSUtils.GEOMETRY_FACTORY.createPoint(movesCoordinates[0]).buffer(1);
        }
        LineString path = JTSUtils.GEOMETRY_FACTORY.createLineString(movesCoordinates);
        return path.buffer(1);
    }
}
