package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.game.mods.hideandseek.HideAndSeekHider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import javafx.util.Pair;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import java.util.List;

public class MobileTreasureHider extends StatisticalHider implements HideAndSeekHider<AngleHint> {

    @Override
    public AngleHint move(SearchPath searchPath) {

        // update GameField
        gameField.commitPlayerMovement(searchPath);

        // first select a new treasure location, then generate the Hints accordingly
        Point newTreasureLocation = generateNewTreasureLocation();
        try{
            gameField.moveTreasure(newTreasureLocation);
            this.treasure = newTreasureLocation;
        } catch (ImpossibleTreasureLocationException e) {
            e.printStackTrace();
        }

        // generate Hints
        List<AngleHint> possibleHints = generateHints(360, searchPath.getLastPoint() );

        // evaluateHints --> use the GameField
        AngleHint hint = eval(possibleHints);

        // commitHint
        gameField.commitHint(hint);
        this.currentPossibleArea = gameField.getPossibleArea();
        // return Hint
        return hint;
    }


    private Point generateNewTreasureLocation() {
        List<Pair<Coordinate,Double>> possibleTreasures = gameField.getWorstPointsOnAllEdges();

        // Todo refine treasure placement , maybe minDistance
        return gf.createPoint(possibleTreasures.get(0).getKey());
    }


    @Override
    protected double rateHint(AngleHintStat ahs) {
        double rating = 0;

        rating += 10 * ( 1 / ahs.getRelativeAreaCutoff());
        rating += 1 * ahs.getDistanceFromNormalAngleLineToTreasure();
        rating += 3 * ahs.getDistanceFromResultingCentroidToTreasure();

        ahs.setRating(rating);
        return rating;
    }


}
