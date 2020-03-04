package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hint.impl.AngleHint;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class HintEvaluator {

    private List<AngleHintStat> hints;
    private List<Pair<Coordinate, Pair<Double, Double>>> treasurePointsOfInterest; //Coordinate, Constant, Angle under PlayerPosition
    private Coordinate playerPosition;
    private Coordinate origin = new Coordinate(0,0);
    private double currentArea;

    //algorithmic parameters
    double minTreasureDistFromOrigin;

    private HintEvaluator(Coordinate player, double currentArea) {
        playerPosition = player;
        treasurePointsOfInterest = new ArrayList<>();
        hints = new ArrayList<>();
        this.currentArea = currentArea;
    }

    public static HintEvaluator initRound(Coordinate player, double areaBefore) {
        HintEvaluator eval = new HintEvaluator(player, areaBefore);
        return eval;
    }

    public void registerPointOfInterest(Pair<Coordinate, Double> point) {
        double angle = Angle.angle(this.playerPosition, point.getKey());
        log.info("Angle: " + Angle.toDegrees(angle));

        // maybe place some additional restrictions here, e.g. Point must be at least x away from player

        Pair<Coordinate, Pair<Double, Double>> checkedInPoint = new Pair<>(point.getKey(), new Pair(point.getValue(), angle));
        treasurePointsOfInterest.add(checkedInPoint);
    }

    public void registerHint(AngleHint hint, double areaAfter) throws InvalidHintException {
        if (verifyHintInRound(hint) == false) {
            throw new InvalidHintException("given Hint does not have the player as Center");
        }


        AngleHintStat ahs = new AngleHintStat(hint, currentArea, areaAfter);
        hints.add(ahs);
    }

    /**
     * Enforces the Distance Constraint, by stableSorting the TreasureLocations > distance to the low indicies
     * Points which are closer than distance will be sorted to the end, beginning with the points with the greatest distance
     *
     * @param distance
     */
    private void enforceMinTreasureDistanceConstraint(double distance){
        treasurePointsOfInterest = new ArrayList<>();
        List<Pair<Coordinate,Pair<Double,Double>>> validPoints = treasurePointsOfInterest.stream().filter( t -> t.getKey().distance(origin) >= distance).collect(Collectors.toList());
        treasurePointsOfInterest.addAll(validPoints);

        //in case the restriction limits the # of possible who conform the constraint to 0
        if(validPoints.isEmpty()){
            log.info("No Point with Distance > " + distance + " found. Addding Points with smaller distance, sorted by distance");
            List<Pair<Coordinate, Pair<Double,Double>>> invalidPoints = treasurePointsOfInterest.stream().filter( t -> t.getKey().distance(origin) > distance).collect(Collectors.toList());
            treasurePointsOfInterest.addAll(invalidPoints);
        }

    }


    public AngleHint evaluateRound() {
        log.info("Evaluating Round with " + treasurePointsOfInterest.size() + " points of interest and " + hints.size() + " possible hints");

        // sort the Constants of each Point from highest Constant to lowest
        treasurePointsOfInterest.sort(new Comparator<Pair<Coordinate, Pair<Double, Double>>>() {
            @Override
            public int compare(Pair<Coordinate, Pair<Double, Double>> coordConstAngle1, Pair<Coordinate, Pair<Double, Double>> coordConstAngle2) {
                return coordConstAngle1.getValue().getKey().compareTo( coordConstAngle2.getValue().getKey());
            }
        }.reversed()); //

        log.info("worst Coordinate is " + treasurePointsOfInterest.get(0));

        //for each hint assign the best treasure in view
        for (AngleHintStat hintStat : hints) {
            for (Pair<Coordinate, Pair<Double, Double>> p : treasurePointsOfInterest) {
                if (hintStat.getHint().getGeometryAngle().inView(p.getKey())) {
                    Pair<Coordinate, Double> worstPointForHint = new Pair<>(p.getKey(), p.getValue().getKey());
                    hintStat.setWorstConstantPoint(worstPointForHint);
                    break;
                }
            }

        }

        // now apply decision in reverse order of importance

        // sort for Area
        hints.sort(Comparator.comparingDouble(AngleHintStat::getRelativeAreaCutoff));

        // sort for Worst Point
        Comparator<AngleHintStat> worstPointComparator = new Comparator<AngleHintStat>() {
            @Override
            public int compare(AngleHintStat angleHintStat1, AngleHintStat angleHintStat2) {
                double diff = angleHintStat1.getWorstConstantPoint().getValue() - angleHintStat2.getWorstConstantPoint().getValue();
                if(diff == 0){
                    return 0;
                }else if(diff <0){
                    return -1;
                }else{
                    return 1;
                }
            }
        };
        hints.sort(worstPointComparator.reversed());

        log.info("the hints in order" + hints.toString());
        AngleHintStat ahs = hints.get(0);
        log.info(ahs.toString());

        return ahs.getHint();
    }

    private boolean verifyHintInRound(AngleHint hint) {
        if (hint.getGeometryAngle().getCenter() != this.playerPosition) {
            return false;
        }
        return true;
    }


}
