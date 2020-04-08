package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.service.preferences.Preference;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.geom.StatusMessageItem;
import com.treasure.hunt.strategy.geom.StatusMessageType;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import static org.locationtech.jts.algorithm.Angle.interiorAngle;

/**
 * A type of {@link Hider}, generating randomly chosen {@link AngleHint}'s
 *
 * @author dorianreineccius
 */
@Preference(name = PreferenceService.MAX_TREASURE_DISTANCE, value = 100)
@Preference(name = PreferenceService.MIN_TREASURE_DISTANCE, value = 100)
@Preference(name = PreferenceService.TREASURE_DISTANCE, value = 100)
@Preference(name = PreferenceService.ANGLE_UPPER_BOUND, value = 2 * Math.PI)
@Preference(name = PreferenceService.ANGLE_LOWER_BOUND, value = Math.PI)
public class RandomAngleHintHider implements Hider<AngleHint> {
    private Point treasurePosition;

    /**
     * @return A random treaasure location via {@link JTSUtils#shuffleTreasure()}.
     */
    @Override
    public Point getTreasureLocation() {
        treasurePosition = JTSUtils.shuffleTreasure();
        return treasurePosition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Point searcherStartPosition) {
    }

    @Override
    public AngleHint move(SearchPath searchPath) {
        Coordinate searcherPos = searchPath.getLastPoint().getCoordinate();
        double upperBound = PreferenceService.getInstance().getPreference(PreferenceService.ANGLE_UPPER_BOUND, Math.PI * 2).doubleValue();
        double lowerBound = PreferenceService.getInstance().getPreference(PreferenceService.ANGLE_LOWER_BOUND, 0).doubleValue();

        GeometryAngle angle = JTSUtils.validRandomAngle(searcherPos, treasurePosition.getCoordinate(), upperBound, lowerBound);
        double angleDegree = interiorAngle(angle.getRight(), angle.getCenter(), angle.getLeft());

        AngleHint angleHint = new AngleHint(
                angle
        );
        angleHint.getStatusMessageItemsToBeAdded().
                add(new StatusMessageItem(StatusMessageType.ANGLE_HINT_DEGREE, String.valueOf(angleDegree)));
        return angleHint;
    }
}
