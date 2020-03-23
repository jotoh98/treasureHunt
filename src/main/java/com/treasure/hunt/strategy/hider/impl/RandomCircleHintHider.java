package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;

public class RandomCircleHintHider implements Hider<CircleHint> {
    public static int MAX_X = 100;
    public static int MAX_Y = 200;
    public static int MAX_RADIUS = 100;

    Point treasureLocation = JTSUtils.createPoint(0, 0);
    CircleHint lastCircleHint = null;

    @Override
    public void init(Point searcherStartPosition) {

    }

    /**
     * If it is the first time, a {@link CircleHint} is requested, this returns a random {@link CircleHint} with
     * a center {@link Coordinate} lying in [-MAX_X,MAX_X)x[-MAX_Y,MAX_Y),
     * with a radius being of [length ((0,0),(x,y)), MAX_RADIUS).
     * <p>
     * If it is not the first time, a {@link CircleHint} is requested, this returns a random {@link CircleHint} with
     * a center lying in the previous given {@link CircleHint} with a valid radius.
     *
     * @param searchPath the {@link SearchPath}, the {@link com.treasure.hunt.strategy.searcher.Searcher} did last
     * @return
     */
    @Override
    public CircleHint move(SearchPath searchPath) {
        Coordinate center;
        double radius;
        if (lastCircleHint == null) {
            center = new Coordinate(MAX_X * (Math.random() * 2 - 1), MAX_Y * (Math.random() * 2 - 1));
            Vector2D movement = new Vector2D(treasureLocation.getCoordinate(), center);
            radius = movement.length() + (MAX_RADIUS - movement.length()) * Math.random();
        } else {
            Vector2D movement = new Vector2D(new Coordinate(0, 0),
                    new Coordinate(0, lastCircleHint.getCircle().getRadius() * Math.random()));
            movement.rotate(2 * Math.PI * Math.random());
            center = new Coordinate(movement.getX() + lastCircleHint.getCircle().x, movement.getY() + lastCircleHint.getCircle().y);
            radius = Math.random() * (lastCircleHint.getCircle().getRadius() - lastCircleHint.getCircle().distance(center));
        }
        return new CircleHint(center, radius);
    }

    @Override
    public Point getTreasureLocation() {
        return treasureLocation;
    }
}
