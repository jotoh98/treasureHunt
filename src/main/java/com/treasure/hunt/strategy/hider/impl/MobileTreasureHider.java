package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekHider;
import com.treasure.hunt.service.preferences.Preference;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.geom.*;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import java.util.List;

/**
 * A Strategy using the {@link StatisticalHider} as Base Implementation,
 * but being able to move the treasure while the game is running and therefore implementing to the {@link HideAndSeekHider} interface
 *
 * The strategy uses the {@link GameField} to maintain its state
 */
@Slf4j
@Preference(name = PreferenceService.HintSize_Preference, value = 180)
@Preference(name = PreferenceService.TreasureLocationX_Preference, value = 70)
@Preference(name = PreferenceService.TreasureLocationY_Preference, value = 70)
@Preference(name = GameField.CircleExtension_Preference, value = 0)
@Preference(name = StatisticalHider.getRelativeAreaCutoffWeight_Preference, value = 5)
@Preference(name = StatisticalHider.DistanceFromNormalAngleLineToTreasureWeight_Preference, value = 2)
@Preference(name = StatisticalHider.DistanceFromResultingCentroidToTreasureWeight_Preference, value = 3)
@Preference(name = MobileTreasureHider.treasureBeforeHintFirst_Preference, value = 1)
public class MobileTreasureHider extends StatisticalHider implements HideAndSeekHider<AngleHint> {

    public static final String treasureBeforeHintFirst_Preference = "pick treasure before hint? 1/0";
    @Override
    public AngleHint move(SearchPath searchPath) {

        log.info(" --- MOVE STARTED ---");
        // update GameField
        gameField.commitPlayerMovement(searchPath);

        AngleHint hint;

        // treasure first, hint second
        if(PreferenceService.getInstance().getPreference(treasureBeforeHintFirst_Preference, 0).intValue() == 1){

            log.debug("treasure first, hint second");
            // new treasure location
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
            hint = eval(possibleHints);

            // commitHint
            gameField.commitHint(hint);

        }else{ // Hint first, treasure second
            log.debug("Hint first, treasure second");
            // generate Hints
            List<AngleHint> possibleHints = generateHints(360, searchPath.getLastPoint());

            // evaluateHints --> use the GameField
            hint = eval(possibleHints);

            // commitHint
            gameField.commitHint(hint);


            // new treasure location
            Point newTreasureLocation = generateNewTreasureLocation();
            log.info("trying to move the Treasure to " + newTreasureLocation);
            try{
                gameField.moveTreasure(newTreasureLocation);
                this.treasure = newTreasureLocation;
            }catch (ImpossibleTreasureLocationException e) {
                log.info("Setting the treasure to new position failed: " + e.getMessage());
            }

        }


        this.currentPossibleArea = gameField.getPossibleArea();
        hint.addAdditionalItem(gameField.getInnerBufferItem());
        //add some status information
        hint.getStatusMessageItemsToBeAdded().add(new StatusMessageItem(StatusMessageType.CURRENT_TREASURE_POSITION, treasure.toString() ));


        log.info(" --- MOVE ENDED ---");
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

        rating += PreferenceService.getInstance().getPreference(StatisticalHider.getRelativeAreaCutoffWeight_Preference, 5).doubleValue() * ( 1 / ahs.getRelativeAreaCutoff());
        rating += PreferenceService.getInstance().getPreference(StatisticalHider.DistanceFromNormalAngleLineToTreasureWeight_Preference, 2).doubleValue() * ahs.getDistanceFromNormalAngleLineToTreasure();
        rating += PreferenceService.getInstance().getPreference(StatisticalHider.DistanceFromResultingCentroidToTreasureWeight_Preference, 3).doubleValue() * ahs.getDistanceFromResultingCentroidToTreasure();


        ahs.setRating(rating);
        return rating;
    }


}
