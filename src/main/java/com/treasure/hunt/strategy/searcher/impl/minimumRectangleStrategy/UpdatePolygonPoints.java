package com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy.MinimumRectangleStrategy.Intersection;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UpdatePolygonPoints {
    /**
     * Tests if the intersection of hintOne and hintTwo lies in all other half-planes of the hints in otherHintsOne,
     * otherHintsTwo and otherHintsThree.
     * If this is the case the intersection gets added to intersectionList, otherwise nothing is done.
     *
     * @param hintOne
     * @param hintTwo
     * @param otherHintsOne
     * @param otherHintsTwo
     * @param otherHintsThree
     */
    static void addIntersectionIfInPoly(HalfPlaneHint hintOne, HalfPlaneHint hintTwo,
                                        List<HalfPlaneHint> otherHintsOne, List<HalfPlaneHint> otherHintsTwo,
                                        List<HalfPlaneHint> otherHintsThree,
                                        List<Intersection> intersectionList,
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
        for (HalfPlaneHint hint : otherHintsThree) {
            if (hint != hintOne && hint != hintTwo) {
                if (!hint.inHalfPlane(intersection)) {
                    return;
                }
            }
        }
        intersectionList.add(new Intersection(intersection, hintOne, hintTwo));
        newPolygonCorners.add(intersection);
    }

    /**
     * When the phase has changed, this method can be called and it returns the new polygon if its not empty.
     * If it is empty, null is returned.
     *
     * @param oldPhaseHints     The phase hints of the previous phase
     * @param oldPhaseRectangle The rectangle of the previous phase
     * @return the new polygon if it isn't empty, null otherwise
     */
    static Polygon updatePolygonPoints(List<Intersection> polygonPoints, List<HalfPlaneHint> oldObtainedHints,
                                       List<HalfPlaneHint> newObtainedHints, ArrayList<HalfPlaneHint> oldPhaseHints,
                                       ArrayList<HalfPlaneHint> newPhaseHints, Polygon oldPhaseRectangle) {
        List<MinimumRectangleStrategy.Intersection> newPolygonPoints = new LinkedList<>();

        ArrayList<Coordinate> newPolygonCorners;
        if (polygonPoints.size() != 0) {
            newPolygonCorners = new ArrayList<>(polygonPoints.size());
        } else {
            newPolygonCorners = new ArrayList<>();
        }

        // iterate over the old intersections, and look if they can be reused:
        List<Intersection> removeFromPolygonPoints = new LinkedList<>();
        for (Intersection intersection : polygonPoints) {
            if (!(oldPhaseHints.contains(intersection.getHintOne()) ||
                    oldPhaseHints.contains(intersection.getHintTwo()))) {
                boolean isInNewPolygon = true;
                for (HalfPlaneHint testHintHP : newObtainedHints) {
                    if (!testHintHP.inHalfPlane(intersection.getCoordinate())) {
                        isInNewPolygon = false;
                    }
                }
                for (HalfPlaneHint testHintHP : newPhaseHints) {
                    if (!testHintHP.inHalfPlane(intersection.getCoordinate())) {
                        isInNewPolygon = false;
                    }
                }
                if (isInNewPolygon) {
                    newPolygonCorners.add(intersection.getCoordinate());
                }
            } else {
                removeFromPolygonPoints.add(intersection);
                //polygonPoints.remove(intersection);
            }
        }
        polygonPoints.removeAll(removeFromPolygonPoints);

        // calculate new intersections and look if they are inside the polygon
        for (HalfPlaneHint hintOne : oldObtainedHints) {
            for (HalfPlaneHint hintTwo : newObtainedHints) {
                addIntersectionIfInPoly(hintOne, hintTwo, oldObtainedHints,
                        newObtainedHints, newPhaseHints, newPolygonPoints, newPolygonCorners);
            }
            for (HalfPlaneHint hintTwo : newPhaseHints) {
                addIntersectionIfInPoly(hintOne, hintTwo, oldObtainedHints,
                        newObtainedHints, newPhaseHints, newPolygonPoints, newPolygonCorners);
            }
        }
        for (HalfPlaneHint hintOne : newObtainedHints) {
            for (HalfPlaneHint hintTwo : newObtainedHints) {
                addIntersectionIfInPoly(hintOne, hintTwo, oldObtainedHints,
                        newObtainedHints, newPhaseHints, newPolygonPoints, newPolygonCorners);
            }
            for (HalfPlaneHint hintTwo : newPhaseHints) {
                addIntersectionIfInPoly(hintOne, hintTwo, oldObtainedHints,
                        newObtainedHints, newPhaseHints, newPolygonPoints, newPolygonCorners);
            }
        }
        for (HalfPlaneHint hintOne : newPhaseHints) {
            for (HalfPlaneHint hintTwo : newPhaseHints) {
                addIntersectionIfInPoly(hintOne, hintTwo, oldObtainedHints,
                        newObtainedHints, newPhaseHints, newPolygonPoints, newPolygonCorners);
            }
        }
        polygonPoints.addAll(newPolygonPoints);

        Geometry newPolygon = JTSUtils.GEOMETRY_FACTORY.createMultiPointFromCoords(
                newPolygonCorners.toArray(new Coordinate[]{}));
        newPolygon = newPolygon.convexHull();
        newPolygon = newPolygon.difference(oldPhaseRectangle);

        if (newPolygon.getArea() == 0) {
            return null;
        }

        return (Polygon) newPolygon;
    }
}
