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
 * @author bsen
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

    private HalfPlaneHint lastHint; // if it is not null it also gets drawn by this HalfPlaneHint

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

    public HalfPlaneHint(Coordinate leftPoint, Coordinate rightPoint, HalfPlaneHint lastHint) {
        this(leftPoint, rightPoint);
        this.lastHint = lastHint;
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

    public LineString getHalfPlaneLine() {
        if (halfPlaneLine == null) {
            Vector2D l_to_r = new Vector2D(leftPoint, rightPoint);
            Vector2D r_to_l = new Vector2D(rightPoint, leftPoint);

            l_to_r = l_to_r.multiply(visual_extent / l_to_r.length());
            r_to_l = r_to_l.multiply(visual_extent / r_to_l.length());

            Coordinate extendedL = new Coordinate(
                    rightPoint.x + r_to_l.getX(),
                    rightPoint.y + r_to_l.getY()
            );
            Coordinate extendedR = new Coordinate(
                    leftPoint.x + l_to_r.getX(),
                    leftPoint.y + l_to_r.getY()
            );
            Coordinate[] line = new Coordinate[]{extendedL, extendedR};
            halfPlaneLine = GEOMETRY_FACTORY.createLineString(line);
        }
        return halfPlaneLine;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public List<GeometryItem<?>> getGeometryItems() {
        List<GeometryItem<?>> output = new ArrayList<>();
        output.add(new GeometryItem(getHalfPlaneLine(), GeometryType.HALF_PLANE_LINE));
        if (lastHint != null && lastHint.getHalfPlaneLine() != null) {
            output.add(new GeometryItem(lastHint.getHalfPlaneLine(), GeometryType.HALF_PLANE_LINE_BLUE));
        }

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
