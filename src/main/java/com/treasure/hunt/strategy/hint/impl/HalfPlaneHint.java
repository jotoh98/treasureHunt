package com.treasure.hunt.strategy.hint.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

import java.util.List;

import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.*;

/**
 * @author Rank
 */

public class HalfPlaneHint extends AngleHint {

    @Getter
    private Direction direction;
    // when the line indicated by anglePointLeft and anglePointRight is not horizontal,
    // right and left indicate where the target is (right indicates the target is in positive x-Direction
    // in relationship to the line)
    // when the line is horizontal, up signals the target is in positive y-Direction in relationship
    // to the line (the up and down enumerators are only used when the line is horizontal)
    // left and down respectively

    public HalfPlaneHint(Point anglePointLeft, Point anglePointRight) {
        super(anglePointRight, null, anglePointLeft);
        if (anglePointLeft.getY() == anglePointRight.getY()) {
            if (anglePointLeft.getX() < anglePointRight.getX()) {
                direction = up;
            }
            if (anglePointLeft.getX() > anglePointRight.getX()) {
                direction = down;
            }
            if (anglePointLeft.getX() == anglePointRight.getX()) {
                throw new IllegalArgumentException("anglePointLeft must not equal anglePointRight in the " +
                        "construction of a new HalfPlaneHint");
            }
        }
        if (anglePointLeft.getY() < anglePointRight.getY()) {
            direction = left;
        }
        if (anglePointLeft.getY() > anglePointRight.getY()) {
            direction = right;
        }
    }

    /**
     * This constructor can be used when unsure whether pointOne or pointTwo is the right/left point of the
     * hint but the direction of the hint is known.
     *
     * @param pointOne
     * @param pointTwo
     * @param direction
     */
    public HalfPlaneHint(Point pointOne, Point pointTwo, Direction direction) {
        super(null, null, null);
        switch (direction) {
            case up:
                if (pointOne.getX() < pointTwo.getX()) {
                    anglePointLeft = pointOne;
                    anglePointRight = pointTwo;
                }
                if (pointOne.getX() > pointTwo.getX()) {
                    anglePointLeft = pointTwo;
                    anglePointRight = pointOne;
                }
                if (pointOne.getX() == pointTwo.getX()) {
                    throw new IllegalArgumentException("If the direction is up, the x values of pointOne and pointTwo" +
                            "must not be identical");
                }
                break;
            case down:
                if (pointOne.getX() < pointTwo.getX()) {
                    anglePointLeft = pointTwo;
                    anglePointRight = pointOne;
                }
                if (pointOne.getX() > pointTwo.getX()) {
                    anglePointLeft = pointOne;
                    anglePointRight = pointTwo;
                }
                if (pointOne.getX() == pointTwo.getX()) {
                    throw new IllegalArgumentException("If the direction is down, the x values of pointOne and pointTwo" +
                            "must not be identical");
                }
                break;
            case left:
                if (pointOne.getY() < pointTwo.getY()) {
                    anglePointLeft = pointOne;
                    anglePointRight = pointTwo;
                }
                if (pointOne.getY() > pointTwo.getY()) {
                    anglePointLeft = pointTwo;
                    anglePointRight = pointOne;
                }
                if (pointOne.getY() == pointTwo.getY()) {
                    throw new IllegalArgumentException("If the direction is left, the y values of pointOne and " +
                            "pointTwo must not be identical.");
                }
                break;
            case right:
                if (pointOne.getY() < pointTwo.getY()) {
                    anglePointLeft = pointTwo;
                    anglePointRight = pointOne;
                }
                if (pointOne.getY() > pointTwo.getY()) {
                    anglePointLeft = pointOne;
                    anglePointRight = pointTwo;
                }
                if (pointOne.getY() == pointTwo.getY()) {
                    throw new IllegalArgumentException("If the direction is right, the y values of pointOne and " +
                            "pointTwo must not be identical");
                }
                break;
        }
        this.direction = direction;
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

    public Point getLowerHintPoint() {
        if (anglePointLeft.getY() < anglePointRight.getY()) {
            return anglePointLeft;
        } else {
            return anglePointRight;
        }
    }

    public Point getUpperHintPoint() {
        if (anglePointLeft.getY() < anglePointRight.getY()) {
            return anglePointRight;
        } else {
            return anglePointLeft;
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
