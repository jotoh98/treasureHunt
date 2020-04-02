package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.jts.geom.Circle;
import com.treasure.hunt.service.preferences.Preference;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

/**
 * This kind of {@link Hider} gives a randomly placed and randomly sized valid {@link CircleHint}.
 *
 * @author dorianreineccius
 */
@Preference(name = PreferenceService.MAX_TREASURE_DISTANCE, value = 100)
@Preference(name = PreferenceService.MIN_TREASURE_DISTANCE, value = 0)
@Preference(name = PreferenceService.CIRCLE_INIT_RADIUS, value = 100)
public class RandomCircleHintHider implements Hider<CircleHint> {

    Point treasureLocation = JTSUtils.shuffleTreasure();
    Circle lastCircle = null;

    /**
     * {@inheritDoc}
     */
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
     * @return randomly generated circle
     */
    @Override
    public CircleHint move(SearchPath searchPath) {
        if (lastCircle == null) {
            final double initRadius = PreferenceService.getInstance().getPreference(PreferenceService.CIRCLE_INIT_RADIUS, 100).doubleValue();
            final Coordinate center = JTSUtils.randomInCircle(new Circle(treasureLocation.getCoordinate(), initRadius));
            lastCircle = new Circle(center, initRadius);
        } else {
            lastCircle = JTSUtils.randomContainedCircle(lastCircle, treasureLocation.getCoordinate(), lastCircle.getRadius() / (1 + Math.random() * 2));
        }
        return new CircleHint(lastCircle.copy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point getTreasureLocation() {
        return treasureLocation;
    }
}
