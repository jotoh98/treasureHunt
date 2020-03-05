package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.jts.geom.GeometryAngle;
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
public class RandomAngleHintHider implements Hider<AngleHint> {
    public static final String TREASURE_DISTANCE = "TREASURE_DISTANCE";
    private Point treasurePosition;

    /**
     * @return {@link Point} containing treasure location of [0,100)x[0x100)
     */
    @Override
    public Point getTreasureLocation() {
        Number treasureDistance = PreferenceService.getInstance().getPreference(TREASURE_DISTANCE, 100);
        treasurePosition = JTSUtils.createPoint(Math.random() * treasureDistance.doubleValue(), Math.random() * treasureDistance.doubleValue());
        return treasurePosition;
    }

    @Override
    public void init(Point searcherStartPosition) {
    }

    @Override
    public AngleHint move(SearchPath searchPath) {
        Coordinate searcherPos = searchPath.getLastPoint().getCoordinate();

        GeometryAngle angle = JTSUtils.validRandomAngle(searcherPos, treasurePosition.getCoordinate(), 2 * Math.PI);
        double angleDegree = interiorAngle(angle.getRight(), angle.getCenter(), angle.getLeft());

        AngleHint angleHint = new AngleHint(
                angle
        );
        angleHint.getStatusMessageItemsToBeAdded().
                add(new StatusMessageItem(StatusMessageType.ANGLE_HINT_DEGREE, String.valueOf(angleDegree)));
        return angleHint;
    }
}
