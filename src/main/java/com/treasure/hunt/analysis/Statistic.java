package com.treasure.hunt.analysis;

import com.treasure.hunt.game.Turn;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.utils.ListUtils;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author Trostorff, Daniel
 */
public class Statistic {
    private List<Turn> turns;

    public double getTraceLength() {
        double length = ListUtils
                .consecutive(turns, (turn, turn2) -> turn2.getSearchPath().getLength())
                .reduce(Double::sum)
                .orElse(0d);
        if (turns.size() > 0) {
            length += turns.get(0).getSearchPath().getLength();
        }
        return length;
    }

    public Point getStartPoint() {
        return turns.get(0).getSearchPath().getSearcherStartPoint();
    }

    public Point getTreasureLocation() {
        return turns.get(0).getTreasure().getPoint();
    }

    public double getOptimumSolution() {
        return getStartPoint().distance(getTreasureLocation());
    }

    public double getRunningTimeFactor(Function<Double, Double> method) {
        return getTraceLength() / method.apply(getOptimumSolution());
    }

    public double getSolutionQuotient() {
        return getRunningTimeFactor((Double opt) -> opt);
    }

    public double getQuadraticRunningTimeFactor() {
        return this.getRunningTimeFactor((Double opt) -> opt * opt);
    }

    public double getHintRequests() {
        return turns.size() - 1;
    }

    public double getHintTraceLengthRatio() {
        return getHintRequests() / getTraceLength();
    }

    public List<StatisticObject> calculate(List<Turn> turns) {
        this.turns = new ArrayList<>(turns);
        ArrayList<StatisticObject> statisticObjects = new ArrayList<>(Arrays.asList(
                new StatisticObject(StatisticObject.StatisticInfo.TRACE_LENGTH, getTraceLength()
                ),
                new StatisticObject(StatisticObject.StatisticInfo.SOLUTION_QUOTIENT, getSolutionQuotient()
                ),
                new StatisticObject(StatisticObject.StatisticInfo.HINT_REQUEST, getHintRequests()

                ),
                new StatisticObject(StatisticObject.StatisticInfo.HINT_TRACE_LENGTH_RATION, getHintTraceLengthRatio()
                ),
                new StatisticObject(StatisticObject.StatisticInfo.OPTIMAL_SOLUTION, getOptimumSolution()
                )
        ));
        PreferenceService.getInstance()
                .getPreferences()
                .forEach((key, value) -> statisticObjects.add(new StatisticObject(new StatisticObject.StatisticInfo(key, "Imprted from preferences", Number.class), value)));
        return statisticObjects;
    }
}


