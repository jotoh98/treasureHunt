package com.treasure.hunt.strategy.hint.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import lombok.Getter;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;

/**
 * @author Rank
 */
public class HalfPlaneHint extends AngleHint {

    @Getter
    private Direction direction;    // when the line indicated by halfplanePointOne and halfplanePointTwo is not horizontal,
    // right and left indicate where the target is (right indicates the target is in positive x-Direction
    // in relationship to the line)
    // when the line is horizontal, up signals the target is in positive y-Direction in relationship
    // to the line (the up and down enumerators are only used when the line is horizontal)
    // left and down respectively

    @Getter
    private Coordinate halfPlanePoint;

    private Coordinate center;

    public HalfPlaneHint(Coordinate center, Coordinate halfPlanePoint, Direction direction) {
        super(halfPlanePoint, center, null);
        this.direction = direction;
        this.center = center;
        this.halfPlanePoint = halfPlanePoint;
    }

    // TODO is this necessary ?
    public static HalfPlaneHint angular2correctHalfPlaneHint(AngleHint anglehint) {

        Coordinate P1 = anglehint.getGeometryAngle().getLeft();
        Coordinate P2 = anglehint.getGeometryAngle().getRight();
        Coordinate C = anglehint.getGeometryAngle().getCenter();

        double yPointOne = P1.getY();
        double xPointOne = P1.getX();
        double yCenter = C.getY();
        double xCenter = C.getX();
        double yPointTwo = P2.getY();
        double xPointTwo = P2.getX();

        if (Angle.angleBetweenOriented(P1, C, P2) <= 0) {
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

    @Override
    public List<GeometryItem<?>> getGeometryItems() {
        // TODO implement
        return null;
    }

    // TODO could be simplified
    private Coordinate getLowerHintPoint() {
        if (center.getY() < halfPlanePoint.getY()) {
            return center;
        } else {
            return halfPlanePoint;
        }
    }

    // TODO could be simplified
    private Coordinate getUpperHintPoint() {
        if (center.getY() < halfPlanePoint.getY()) {
            return halfPlanePoint;
        } else {
            return center;
        }
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
