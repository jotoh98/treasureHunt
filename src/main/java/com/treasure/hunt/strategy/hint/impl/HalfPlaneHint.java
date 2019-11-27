package com.treasure.hunt.strategy.hint.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hint.Hint;
import lombok.Value;
import org.locationtech.jts.geom.Point;

import java.util.List;

@Value
public class HalfPlaneHint extends Hint {

    private Direction direction;    // when the line indicated by halfplanePointOne and halfplanePointTwo is not horizontal,
    // right and left indicate where the target is (right indicates the target is in positive x-Direction
    // in relationship to the line)
    // when the line is horizontal, up signals the target is in positive y-Direction in relationship
    // to the line (the up and down enumerators are only used when the line is horizontal)
    // left and down respectively

    private Point pointOne;
    private Point pointTwo;

    @Override
    public List<GeometryItem> getGeometryItems() {
        // TODO implement
        return null;
    }

    public Point getLowerHintPoint() {
        if (pointOne.getY() < pointTwo.getY()) {
            return pointOne;
        } else {
            return pointTwo;
        }
    }

    public Point getUpperHintPoint() {
        if (pointOne.getY() < pointTwo.getY()) {
            return pointTwo;
        } else {
            return pointOne;
        }
    }

    public boolean pointsUpwards() {
        return (direction == Direction.left && getLowerHintPoint().getX() < getUpperHintPoint().getX()) ||
                (direction == Direction.right && getLowerHintPoint().getX() > getUpperHintPoint().getX());
    }

    public boolean pointsDownwards() {
        return (direction == Direction.left && getLowerHintPoint().getX() > getUpperHintPoint().getX()) ||
                (direction == Direction.right && getLowerHintPoint().getX() < getUpperHintPoint().getX());
    }

    public enum Direction {
        right, left, up, down
    }

}
