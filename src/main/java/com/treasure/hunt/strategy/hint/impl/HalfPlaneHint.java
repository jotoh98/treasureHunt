package com.treasure.hunt.strategy.hint.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Point;

import java.util.List;

/**
 * @author Rank
 */
public class HalfPlaneHint extends AngleHint {

    private Direction direction;    // when the line indicated by halfplanePointOne and halfplanePointTwo is not horizontal,
    // right and left indicate where the target is (right indicates the target is in positive x-Direction
    // in relationship to the line)
    // when the line is horizontal, up signals the target is in positive y-Direction in relationship
    // to the line (the up and down enumerators are only used when the line is horizontal)
    // left and down respectively

    private Point halfPlanePoint;

    public HalfPlaneHint(Point center, Point halfPlanePoint, Direction direction) {
        super(null, center, halfPlanePoint);
        this.direction = direction;
        this.center = center;
        this.halfPlanePoint = halfPlanePoint;
    }

    // TODO is this necessary ?
    public static HalfPlaneHint angular2correctHalfPlaneHint(AngleHint anglehint) {

        Point P1 = anglehint.getAnglePointLeft();
        Point P2 = anglehint.getAnglePointRight();
        Point C = anglehint.getCenter();

        double yPointOne = P1.getY();
        double xPointOne = P1.getX();
        double yCenter = C.getY();
        double xCenter = C.getX();
        double yPointTwo = P2.getY();
        double xPointTwo = P2.getX();

        if (Angle.angleBetweenOriented(P1.getCoordinate(), C.getCoordinate(), P2.getCoordinate()) <= 0) {
            throw new IllegalArgumentException("angular2correctHalfPlaneHint was called with an angular Hint bigger" +
                    " than pi or equal to 0.");
        }

        if (yPointOne == yCenter) {
            if (xPointOne > xCenter) {
                return new HalfPlaneHint(P1, C, Direction.up);
            }
            return new HalfPlaneHint(P1, C, Direction.down);
        }

        if (yPointOne < yCenter) {
            return new HalfPlaneHint(P1, C, Direction.right);
        }

        return new HalfPlaneHint(P1, C, Direction.left);
    }

    /**
     * TODO implement
     * <p>
     * {@inheritDoc}
     */
    @Override
    public List<GeometryItem> getGeometryItems() {
        return null;
    }

    // TODO could be simplified
    private Point getLowerHintPoint() {
        if (center.getY() < halfPlanePoint.getY()) {
            return center;
        } else {
            return halfPlanePoint;
        }
    }

    // TODO could be simplified
    private Point getUpperHintPoint() {
        if (center.getY() < halfPlanePoint.getY()) {
            return halfPlanePoint;
        } else {
            return center;
        }
    }

    public Direction getDirection() {
        return this.direction;
    }

    public Point getHalfPlanePoint() {
        return this.halfPlanePoint;
    }

    // TODO is this necessary ?
    public enum Direction {
        right, left, up, down
    }

    public boolean pointsUpwards() {
        return (direction == Direction.left && getLowerHintPoint().getX() < getUpperHintPoint().getX()) ||
                (direction == Direction.right && getLowerHintPoint().getX() > getUpperHintPoint().getX());
    }

    public boolean pointsDownwards() {
        return (direction == Direction.left && getLowerHintPoint().getX() > getUpperHintPoint().getX()) ||
                (direction == Direction.right && getLowerHintPoint().getX() < getUpperHintPoint().getX());
    }

}
