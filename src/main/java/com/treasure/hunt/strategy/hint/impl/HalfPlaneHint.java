package com.treasure.hunt.strategy.hint.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import lombok.Getter;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Point;

import java.util.List;

public class HalfPlaneHint extends AngleHint {

    @Getter
    private Direction direction;    // when the line indicated by halfplanePointOne and halfplanePointTwo is not horizontal,
    // right and left indicate where the target is (right indicates the target is in positive x-Direction
    // in relationship to the line)
    // when the line is horizontal, up signals the target is in positive y-Direction in relationship
    // to the line (the up and down enumerators are only used when the line is horizontal)
    // left and down respectively

    @Getter
    private Point halfPlanePoint;

    public HalfPlaneHint(Point center, Point halfPlanePoint, Direction direction) {
        super(null, center, halfPlanePoint);
        this.direction = direction;
        this.center = center;
        this.halfPlanePoint = halfPlanePoint;
    }

    @Override
    public List<GeometryItem> getGeometryItems() {
        // TODO implement
        return null;
    }

    // TODO could be simplified
    public Point getLowerHintPoint() {
        if (center.getY() < halfPlanePoint.getY()) {
            return center;
        } else {
            return halfPlanePoint;
        }
    }

    // TODO could be simplified
    public Point getUpperHintPoint() {
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
