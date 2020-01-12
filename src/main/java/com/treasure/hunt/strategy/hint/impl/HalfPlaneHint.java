package com.treasure.hunt.strategy.hint.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;
import java.util.List;

import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.*;
import static com.treasure.hunt.utils.JTSUtils.GEOMETRY_FACTORY;

/**
 * @author Rank
 */

@AllArgsConstructor
public class HalfPlaneHint extends Hint {
    static final double visual_extent = 1000;
    @Getter
    Coordinate leftPoint;
    @Getter
    Coordinate rightPoint;
    private Polygon halfPlanePoly = null;
    private LineString halfPlaneLine = null;
    @Getter
    private Direction direction;
    // when the line indicated by anglePointLeft and anglePointRight is not horizontal,
    // right and left indicate where the target is (right indicates the target is in positive x-Direction
    // in relationship to the line)
    // when the line is horizontal, up signals the target is in positive y-Direction in relationship
    // to the line (the up and down enumerators are only used when the line is horizontal)
    // left and down respectively

    public HalfPlaneHint(Coordinate leftPoint, Coordinate rightPoint) {
        Direction dir = null;
        this.leftPoint = leftPoint;
        this.rightPoint = rightPoint;

        if (leftPoint.getY() == rightPoint.getY()) {
            if (leftPoint.getX() < rightPoint.getX()) {
                dir = up;
            }
            if (leftPoint.getX() > rightPoint.getX()) {
                dir = down;
            }
            if (leftPoint.getX() == rightPoint.getX()) {
                throw new IllegalArgumentException("anglePointLeft must not equal anglePointRight in the " +
                        "construction of a new HalfPlaneHint");
            }
        }
        if (leftPoint.getY() < rightPoint.getY()) {
            dir = left;
        }
        if (leftPoint.getY() > rightPoint.getY()) {
            dir = right;
        }
        direction = dir;
    }

    /**
     * This constructor can be used when unsure whether pointOne or pointTwo is the right/left point of the
     * hint but the direction of the hint is known.
     *
     * @param pointOne
     * @param pointTwo
     * @param direction
     */
    public HalfPlaneHint(Coordinate pointOne, Coordinate pointTwo, Direction direction) {
        Coordinate rightCoord = null;
        Coordinate leftCoord = null;
        switch (direction) {
            case up:
                if (pointOne.getX() < pointTwo.getX()) {
                    leftCoord = pointOne;
                    rightCoord = pointTwo;
                }
                if (pointOne.getX() > pointTwo.getX()) {
                    leftCoord = pointTwo;
                    rightCoord = pointOne;
                }
                if (pointOne.getX() == pointTwo.getX()) {
                    throw new IllegalArgumentException("If the direction is up, the x values of pointOne and pointTwo" +
                            "must not be identical");
                }
                break;
            case down:
                if (pointOne.getX() < pointTwo.getX()) {
                    leftCoord = pointTwo;
                    rightCoord = pointOne;
                }
                if (pointOne.getX() > pointTwo.getX()) {
                    leftCoord = pointOne;
                    rightCoord = pointTwo;
                }
                if (pointOne.getX() == pointTwo.getX()) {
                    throw new IllegalArgumentException("If the direction is down, the x values of pointOne and pointTwo" +
                            "must not be identical");
                }
                break;
            case left:
                if (pointOne.getY() < pointTwo.getY()) {
                    leftCoord = pointOne;
                    rightCoord = pointTwo;
                }
                if (pointOne.getY() > pointTwo.getY()) {
                    leftCoord = pointTwo;
                    rightCoord = pointOne;
                }
                if (pointOne.getY() == pointTwo.getY()) {
                    throw new IllegalArgumentException("If the direction is left, the y values of pointOne and " +
                            "pointTwo must not be identical.");
                }
                break;
            case right:
                if (pointOne.getY() < pointTwo.getY()) {
                    leftCoord = pointTwo;
                    rightCoord = pointOne;
                }
                if (pointOne.getY() > pointTwo.getY()) {
                    leftCoord = pointOne;
                    rightCoord = pointTwo;
                }
                if (pointOne.getY() == pointTwo.getY()) {
                    throw new IllegalArgumentException("If the direction is right, the y values of pointOne and " +
                            "pointTwo must not be identical");
                }
                break;
        }
        rightPoint = rightCoord;
        leftPoint = leftCoord;
        this.direction = direction;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public List<GeometryItem<?>> getGeometryItems() {
        List<GeometryItem<?>> output = new ArrayList<>();
        if (halfPlaneLine == null) {
            //TODO nachfragen ob andere bessere idee haben

            Vector2D l_to_r = new Vector2D(leftPoint, rightPoint);
            Vector2D r_to_l = new Vector2D(rightPoint, leftPoint);

            l_to_r = l_to_r.multiply(visual_extent / l_to_r.length());
            r_to_l = r_to_l.multiply(visual_extent / r_to_l.length());
            //test
            System.out.println("extended lines:");
            System.out.println(l_to_r);
            System.out.println(r_to_l);
            //end test

            Coordinate extendedL = new Coordinate(
                    rightPoint.x + r_to_l.getX(),
                    rightPoint.y + r_to_l.getY()
            );
            Coordinate extendedR = new Coordinate(
                    leftPoint.x + l_to_r.getX(),
                    leftPoint.y + l_to_r.getY()
            );
            Vector2D extended_l_to_r = new Vector2D(extendedL, extendedR);
            Vector2D extended_r_to_l = new Vector2D(extendedR, extendedL);

            //Coordinate polygon
            Coordinate firstPointPoly = null, secondPointPoly = null; // third and forth are left and right
            switch (direction) {
                case up:
                    firstPointPoly = new Coordinate(extendedR.x, extendedR.y - visual_extent);
                    secondPointPoly = new Coordinate(extendedL.x, extendedL.y - visual_extent);
                    break;
                case down:
                    firstPointPoly = new Coordinate(extendedR.x, extendedR.y + visual_extent);
                    secondPointPoly = new Coordinate(extendedL.x, extendedL.y + visual_extent);
                    break;
                case left:
                case right:
                    extended_r_to_l = extended_r_to_l.rotateByQuarterCircle(1);
                    firstPointPoly = new Coordinate(extendedR.x + extended_r_to_l.getX(),
                            extendedR.y + extended_r_to_l.getY());
                    extended_l_to_r = extended_l_to_r.rotateByQuarterCircle(3);
                    secondPointPoly = new Coordinate(extendedL.x + extended_l_to_r.getX(),
                            extendedL.y + extended_l_to_r.getY());
            }

            Coordinate[] polyShell = new Coordinate[]{firstPointPoly, secondPointPoly,
                    extendedL, extendedR, firstPointPoly};
            Coordinate[] line = new Coordinate[]{extendedL, extendedR};
            halfPlaneLine = GEOMETRY_FACTORY.createLineString(line);
        }

        //output.add(new GeometryItem(GEOMETRY_FACTORY.createPoint(leftPoint), GeometryType.HALF_PLANE_POINT_LEFT));
        //output.add(new GeometryItem(GEOMETRY_FACTORY.createPoint(rightPoint), GeometryType.HALF_PLANE_POINT_RIGHT));

        //output.add(new GeometryItem(halfPlanePoly, GeometryType.HALF_PLANE));
        output.add(new GeometryItem(halfPlaneLine, GeometryType.HALF_PLANE_LINE));

        return output;
    }

    public Coordinate getLowerHintPoint() {
        if (leftPoint.getY() < rightPoint.getY()) {
            return leftPoint;
        } else {
            return rightPoint;
        }
    }

    public Coordinate getUpperHintPoint() {
        if (leftPoint.getY() < rightPoint.getY()) {
            return rightPoint;
        } else {
            return leftPoint;
        }
    }

    public boolean pointsUpwards() {
        return (getDirection() == left && getLowerHintPoint().getX() < getUpperHintPoint().getX()) ||
                (getDirection() == right && getLowerHintPoint().getX() > getUpperHintPoint().getX());
    }

    public boolean pointsDownwards() {
        return (getDirection() == left && getLowerHintPoint().getX() > getUpperHintPoint().getX()) ||
                (getDirection() == right && getLowerHintPoint().getX() < getUpperHintPoint().getX());
    }

    public enum Direction {
        right, left, up, down
    }
}
