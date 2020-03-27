package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekHider;
import com.treasure.hunt.jts.geom.Line;
import com.treasure.hunt.service.preferences.Preference;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.geom.*;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;
import org.locationtech.jts.operation.distance.DistanceOp;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

@Slf4j
@Preference(name = PreferenceService.HintSize_Preference, value = 180)
@Preference(name = PreferenceService.TreasureLocationX_Preference, value = 70)
@Preference(name = PreferenceService.TreasureLocationY_Preference, value = 70)
@Preference(name = GameField.CircleExtension_Preference, value = 0)
public class MobileTreasureHider extends StatisticalHider implements HideAndSeekHider<AngleHint> {

    @Override
    public AngleHint move(SearchPath searchPath) {

        // update GameField
        gameField.commitPlayerMovement(searchPath);

        // first select a new treasure location, then generate the Hints accordingly
        Point newTreasureLocation = generateNewTreasureLocation();
        log.info("trying to move the Treasure to " + newTreasureLocation);

        try{
            gameField.moveTreasure(newTreasureLocation);
            this.treasure = newTreasureLocation;
        }catch (ImpossibleTreasureLocationException e) {
            log.info("Setting the treasure to new position failed: " + e.getMessage());
        }

        // generate Hints
        List<AngleHint> possibleHints = generateHints(360, searchPath.getLastPoint());

        // evaluateHints --> use the GameField
        AngleHint hint = eval(possibleHints);

        // commitHint
        gameField.commitHint(hint);
        this.currentPossibleArea = gameField.getPossibleArea();

        //add some status information
        hint.getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.CURRENT_TREASURE_POSITION, treasure.toString() ));

        // return Hint
        return hint;
    }

    private Point generateNewTreasureLocation() {
        List<Pair<Coordinate, Double>> possibleTreasures = gameField.getWorstPointsOnAllEdges();

        // Todo refine treasure placement , maybe minDistance
        return gf.createPoint(possibleTreasures.get(0).getKey());
    }

    @Override
    protected double rateHint(AngleHintStatistic ahs) {
        double rating = 0;

        rating += 10 * (1 / ahs.getRelativeAreaCutoff());
        rating += 1 * ahs.getDistanceFromNormalAngleLineToTreasure();
        rating += 3 * ahs.getDistanceFromResultingCentroidToTreasure();

        ahs.setRating(rating);
        return rating;
    }


}
