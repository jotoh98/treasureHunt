package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.service.preferences.Preference;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;

/**
 * A type of {@link Hider}, generating a valid {@link AngleHint},
 * which contains an angle of the provided {@link PreferenceService#ANGLE} (standard is {@link Math#PI})
 * with his right wing pointing at the treasure.
 *
 * @author dorianreineccius
 */
@Preference(name = PreferenceService.MAX_TREASURE_DISTANCE, value = 100)
@Preference(name = PreferenceService.HintSize_Preference, value = 180)
public class RightHandAngleHintHider implements Hider<AngleHint> {
    private Point treasurePosition;

    /**
     * @return A random treasure location via {@link JTSUtils#shuffleTreasure()}.
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

    /**
     * @param searchPath the {@link SearchPath}, the {@link com.treasure.hunt.strategy.searcher.Searcher} did last
     * @return a valid {@link AngleHint},
     * which contains an angle of the provided {@link PreferenceService#ANGLE} (standard is {@link Math#PI})
     * with his right wing pointing at the treasure.
     */
    @Override
    public AngleHint move(SearchPath searchPath) {
        Coordinate searcherPos = searchPath.getLastPoint().getCoordinate();
        double angle = Angle.toRadians(PreferenceService.getInstance().getPreference(PreferenceService.HintSize_Preference, 180).doubleValue());

        if (PreferenceService.getInstance().getPreference(PreferenceService.HintSize_Preference, 180).intValue() == 180){
            return new HalfPlaneHint(searcherPos, treasurePosition.getCoordinate());
        }
        Vector2D vector2D = new Vector2D(searcherPos, treasurePosition.getCoordinate());
        vector2D = vector2D.rotate(angle);

        return new AngleHint(
                treasurePosition.getCoordinate(),
                searcherPos,
                new Coordinate(searcherPos.x + vector2D.toCoordinate().x, searcherPos.y + vector2D.toCoordinate().y)
        );
    }
}
