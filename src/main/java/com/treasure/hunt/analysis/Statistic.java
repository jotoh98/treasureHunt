package com.treasure.hunt.analysis;

import com.treasure.hunt.game.Turn;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.utils.ListUtils;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * Get the locally optimal trace length for the searcher.
     * That's the trace length of the path visited plus the optimal distance between the last searcher position and the
     * treasure.
     *
     * @return locally optimal trace length
     */
    public double getLocalOptimumSolution() {
        Turn lastTurn = turns.get(turns.size() - 1);
        double remainder = lastTurn.getSearchPath().getLastPoint().distance(lastTurn.getTreasureLocation());
        return getTraceLength() + remainder;
    }

    public Point getStartPoint() {
        return turns.get(0).getSearchPath().getFirstPoint();
    }

    public Point getTreasureLocation() {
        return turns.get(0).getTreasureLocation();
    }

    public double getOptimumSolution() {
        return getStartPoint().distance(getTreasureLocation());
    }

    /**
     * Calculate the solution quotient.
     * This is the quotient between the local and the global trace length optimum.
     *
     * @return solution quotient
     */
    public double getSolutionQuotient() {
        return getLocalOptimumSolution() / getOptimumSolution();
    }

    public int getHintRequests() {
        if (turns.size() > 1 && turns.get(turns.size() - 1).getHint() == null) {
            return turns.size() - 2;
        }
        return turns.size() - 1;
    }

    public double getHintTraceLengthRatio() {
        final double traceLength = getTraceLength();
        if (traceLength == 0) {
            return 1;
        }
        return getHintRequests() / traceLength;
    }

    public static List<Number> filterBy(List<StatisticsWithId> list, StatisticObject.StatisticInfo info) {
        return list
                .stream()
                .flatMap(statisticsWithId -> statisticsWithId.getStatisticObjects().stream())
                .filter(statisticObject ->
                        statisticObject
                                .getStatisticInfo()
                                .equals(info)
                )
                .map(StatisticObject::getValue)
                .collect(Collectors.toList());

    }

    public List<StatisticObject> calculate(List<Turn> turns, boolean finished) {
        this.turns = new ArrayList<>(turns);
        ArrayList<StatisticObject> statisticObjects = new ArrayList<>(Arrays.asList(
                new StatisticObject(StatisticObject.StatisticInfo.TRACE_LENGTH, getTraceLength()),
                new StatisticObject(StatisticObject.StatisticInfo.LOCAL_OPTIMUM, getLocalOptimumSolution()),
                new StatisticObject(StatisticObject.StatisticInfo.SOLUTION_QUOTIENT, getSolutionQuotient()),
                new StatisticObject(StatisticObject.StatisticInfo.HINT_REQUEST, getHintRequests()),
                new StatisticObject(StatisticObject.StatisticInfo.HINT_TRACE_LENGTH_RATION, getHintTraceLengthRatio()),
                new StatisticObject(StatisticObject.StatisticInfo.OPTIMAL_SOLUTION, getOptimumSolution()),
                new StatisticObject(StatisticObject.StatisticInfo.FINISHED_AND_FOUND, finished? 1:0)
        ));
        PreferenceService.getInstance()
                .getPreferences()
                .forEach((key, value) -> statisticObjects.add(new StatisticObject(new StatisticObject.StatisticInfo(key, "Imported from preferences", Number.class), value)));
        return statisticObjects;
    }


}


