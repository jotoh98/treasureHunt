package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.geom.StatusMessageItem;
import com.treasure.hunt.strategy.geom.StatusMessageType;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import com.treasure.hunt.utils.Preference;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;

/**
 * This type of {@link Hider} returns a random {@link HalfPlaneHint},
 * which is correct.
 *
 * @author Rank
 */
@Preference(name = RandomHalfPlaneHintHider.TREASURE_DISTANCE, value = 1000)
public class RandomHalfPlaneHintHider implements Hider<HalfPlaneHint> {
    public static final String TREASURE_DISTANCE = "treasure distance";
    HalfPlaneHint lastHint = null;
    StatusMessageItem visualisationMessage;
    private Point treasurePos = null;
    private boolean firstMove = true;
    private boolean secondMove = true;

    private int moveIndex = 0; // just for testing

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
        //test todo rm
        StatusMessageItem statusTestOne = new StatusMessageItem(StatusMessageType.ANGLE_HINT_DEGREE, "this is a status message");
        StatusMessageItem statusTestTwo = new StatusMessageItem(StatusMessageType.ANGLE_HINT_DEGREE, "this is a status message");
        switch (moveIndex) {
            case 0:
                newHint.getStatusMessageItemsToBeAdded().add(statusTestOne);
                break;
            case 1:
                newHint.getStatusMessageItemsToBeRemoved().add(statusTestOne);
                break;
            case 2:
                newHint.getStatusMessageItemsToBeAdded().add(statusTestTwo);
                break;
        }
        moveIndex++;
        //test end
        lastHint = newHint;
        return newHint;
    }

    /**
     * @return {@link Point} containing treasure location
     */
    @Override
    public Point getTreasureLocation() {
        if (treasurePos == null) {
            treasurePos = JTSUtils.GEOMETRY_FACTORY.createPoint(
                    Vector2D.create(
                            PreferenceService.getInstance().getPreference(RandomHalfPlaneHintHider.TREASURE_DISTANCE, 1000).doubleValue(), 0)
                            .rotate(2 * Math.PI * Math.random())
                            .toCoordinate()
            );
        }
        return treasurePos;
    }
}
