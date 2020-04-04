package com.treasure.hunt.strategy.hint.impl;

import com.treasure.hunt.jts.geom.HalfPlane;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;

import java.util.ArrayList;
import java.util.List;

import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.down;
import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.up;

/**
 * A special ase of {@link AngleHint} with 180 degrees or {@link Math#PI} radians,
 * defining a {@link HalfPlane} in which the treasure lies
 *
 * @author bsen
 */

public class HalfPlaneHint extends AngleHint {
    private HalfPlane halfPlaneTheTreasureIsNotIn = null;
    /**
     * when the line indicated by anglePointLeft and anglePointRight is not horizontal,
     * right and left indicate where the target is (right indicates the target is in positive x-Direction
     * in relationship to the line)
     * when the line is horizontal, up signals the target is in positive y-Direction in relationship
     * to the line (the up and down enumerators are only used when the line is horizontal)
     * left and down respectively
     */
    private Direction direction = null;
    private boolean visualisation = true;

    public HalfPlaneHint(Coordinate center, Coordinate right, boolean visualisation) {
        this(center, right);
        this.visualisation = visualisation;
    }

    public HalfPlaneHint(Coordinate center, Coordinate right) {
        super(right, center, new Coordinate(2 * center.x - right.x, 2 * center.y - right.y));
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
        super(new Coordinate(), new Coordinate(), new Coordinate());

        Coordinate right = null;
        Coordinate center = null;
        switch (direction) {
            case up:
                if (pointOne.getX() < pointTwo.getX()) {
                    center = pointOne;
                    right = pointTwo;
                }
                if (pointOne.getX() > pointTwo.getX()) {
                    center = pointTwo;
                    right = pointOne;
                }
                if (pointOne.getX() == pointTwo.getX()) {
                    throw new IllegalArgumentException("If the direction is up, the x values of pointOne and pointTwo" +
                            "must not be identical");
                }
                break;
            case down:
                if (pointOne.getX() < pointTwo.getX()) {
                    center = pointTwo;
                    right = pointOne;
                }
                if (pointOne.getX() > pointTwo.getX()) {
                    center = pointOne;
                    right = pointTwo;
                }
                if (pointOne.getX() == pointTwo.getX()) {
                    throw new IllegalArgumentException("If the direction is down, the x values of pointOne and pointTwo" +
                            "must not be identical");
                }
                break;
            case left:
                if (pointOne.getY() < pointTwo.getY()) {
                    center = pointOne;
                    right = pointTwo;
                }
                if (pointOne.getY() > pointTwo.getY()) {
                    center = pointTwo;
                    right = pointOne;
                }
                if (pointOne.getY() == pointTwo.getY()) {
                    throw new IllegalArgumentException("If the direction is left, the y values of pointOne and " +
                            "pointTwo must not be identical.");
                }
                break;
            case right:
                if (pointOne.getY() < pointTwo.getY()) {
                    center = pointTwo;
                    right = pointOne;
                }
                if (pointOne.getY() > pointTwo.getY()) {
                    center = pointOne;
                    right = pointTwo;
                }
                if (pointOne.getY() == pointTwo.getY()) {
                    throw new IllegalArgumentException("If the direction is right, the y values of pointOne and " +
                            "pointTwo must not be identical");
                }
                break;
        }
        geometryAngle.setRight(right);
        geometryAngle.setCenter(center);
        geometryAngle.setLeft(new Coordinate(2 * center.x - right.x, 2 * center.y - right.y));
        this.direction = direction;
    }

    public Direction getDirection() {
        if (direction != null) {
            return direction;
        }

        if (JTSUtils.doubleEqual(getCenter().getY(), getRight().getY())) {
            if (getCenter().getX() < getRight().getX()) {
                direction = up;
            }
            if (getCenter().getX() > getRight().getX()) {
                direction = down;
            }
            if (getCenter().getX() == getRight().getX()) {
                throw new IllegalArgumentException("getCenter() must not equal getRight() in the " +
                        "calculation of the direction");
            }
        }
        if (getCenter().getY() < getRight().getY()) {
            direction = Direction.left;
        }
        if (getCenter().getY() > getRight().getY()) {
            direction = Direction.right;
        }
        return direction;
    }

    public Coordinate getCenter() {
        return geometryAngle.getCenter();
    }

    public Coordinate getRight() {
        return geometryAngle.getRight();
    }

    public Coordinate getLeft() {
        return geometryAngle.getLeft();
    }

    public HalfPlane getHalfPlaneTheTreasureIsNotIn() {
        if (halfPlaneTheTreasureIsNotIn == null) {
            halfPlaneTheTreasureIsNotIn = new HalfPlane(getCenter(), getRight(), true);
        }
        return halfPlaneTheTreasureIsNotIn;
    }

    public LineSegment getHalfPlaneLine() {
        return new LineSegment(getCenter(), getRight());
    }

    public LineString getHalfPlaneLineString() {
        return JTSUtils.GEOMETRY_FACTORY.createLineString(new Coordinate[]{getCenter(), getRight()});
    }

    public boolean inHalfPlane(Coordinate coordinate) {
        return !getHalfPlaneTheTreasureIsNotIn().inside(coordinate);
    }

    public boolean approximatelyInHalfPlane(Coordinate coordinate) {
        double angleHintLine = new LineSegment(getCenter(), getRight()).angle();
        double angleCenterP = new LineSegment(getCenter(), coordinate).angle();
        double halfPlaneToPAngle = Angle.normalizePositive((angleCenterP - angleHintLine));
        return halfPlaneToPAngle < Math.PI || JTSUtils.doubleEqualApproximately(halfPlaneToPAngle, 0) ||
                JTSUtils.doubleEqualApproximately(halfPlaneToPAngle, Math.PI) ||
                JTSUtils.doubleEqualApproximately(halfPlaneToPAngle, Math.PI * 2);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public List<GeometryItem<?>> getGeometryItems() {
        if (visualisation) {
            List<GeometryItem<?>> output = new ArrayList<>();
            output.add(new GeometryItem(getHalfPlaneTheTreasureIsNotIn(), GeometryType.HALF_PLANE_CURRENT_RED));
            return output;
        } else {
            return new ArrayList<>(0);
        }
    }

    public Coordinate getLowerHintPoint() {
        if (getCenter().getY() < getRight().getY()) {
            return getCenter();
        } else {
            return getRight();
        }
    }

    public Coordinate getUpperHintPoint() {
        if (getCenter().getY() < getRight().getY()) {
            return getRight();
        } else {
            return getCenter();
        }
    }

    public boolean pointsUpwards() {
        return (getDirection() == Direction.left && getLowerHintPoint().getX() < getUpperHintPoint().getX()) ||
                (getDirection() == Direction.right && getLowerHintPoint().getX() > getUpperHintPoint().getX());
    }

    public boolean pointsDownwards() {
        return (getDirection() == Direction.left && getLowerHintPoint().getX() > getUpperHintPoint().getX()) ||
                (getDirection() == Direction.right && getLowerHintPoint().getX() < getUpperHintPoint().getX());
    }

    public enum Direction {
        right, left, up, down
    }
}
