package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.service.preferences.Preference;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.geom.StatusMessageItem;
import com.treasure.hunt.strategy.geom.StatusMessageType;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

/**
 * This type of {@link Hider} returns a random {@link HalfPlaneHint},
 * which is correct.
 *
 * @author Rank
 */
@Preference(name = PreferenceService.MAX_TREASURE_DISTANCE, value = 100)
public class RandomHalfPlaneHintHider implements Hider<HalfPlaneHint> {
    public static final String TREASURE_DISTANCE = "treasure distance";
    double xmax = 1000;
    double ymax = 1000;
    HalfPlaneHint lastHint = null;
    StatusMessageItem visualisationMessage;
    private Point treasurePos = null;
    private boolean firstMove = true;
    private boolean secondMove = true;

    /**
     * @param searcherStartPosition the {@link Searcher} starting position,
     *                              he will initialized on.
     */
    @Override
    public void init(Point searcherStartPosition) {
        visualisationMessage = new StatusMessageItem(
                StatusMessageType.EXPLANATION_VISUALISATION_HIDER,
                "This hider (RandomHalfPlaneHintHider) does not show the current hint."
        );
    }


    /**
     * @param movement the {@link SearchPath}, the {@link com.treasure.hunt.strategy.searcher.Searcher} did last
     * @return T a (new) hint.
     */
    @Override
    public HalfPlaneHint move(SearchPath movement) {
        Point searcherPos = movement.getLastPoint();

        double randomAngle = Math.random() * -Math.PI; // Angle between treasurePosition searcherPosition and
        // AnglePointRight
        double rightAngle = Angle.angle(searcherPos.getCoordinate(), treasurePos.getCoordinate()) + randomAngle;
        double rightX = searcherPos.getX() + Math.cos(rightAngle);
        double rightY = searcherPos.getY() + Math.sin(rightAngle);

        HalfPlaneHint newHint = new HalfPlaneHint(searcherPos.getCoordinate(), new Coordinate(rightX, rightY),
                false);
        if (firstMove) {
            firstMove = false;
            newHint.getStatusMessageItemsToBeAdded().add(visualisationMessage);
        } else {
            if (secondMove) {
                secondMove = false;
                newHint.getStatusMessageItemsToBeRemoved().add(visualisationMessage);
            }
        }
        lastHint = newHint;
        return newHint;
    }

    /**
     * @return {@link Point} containing treasure location
     */
    @Override
    public Point getTreasureLocation() {
        treasurePos = JTSUtils.shuffleTreasure();
        return treasurePos;
    }
}
