package com.treasure.hunt.analysis;

import com.treasure.hunt.game.Turn;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.searcher.SearchPath;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class to calculate various statistics.
 *
 * @author Trostorff, Daniel
 */
public class Statistic {
    private List<Turn> turns;

    /**
     * @return The length of the trace, the {@link com.treasure.hunt.strategy.searcher.Searcher} ran yet.
     */
    public double getTraceLength() {
        return turns.stream()
                .map(Turn::getSearchPath)
                .mapToDouble(SearchPath::getLength)
                .reduce(Double::sum)
                .orElse(0d);
    }

    /**
     * Get the locally optimal trace length for the searcher.
     * That's the trace length of the path visited plus the optimal distance between the last searcher position and the
     * treasure.
     *
     * @return locally optimal trace length
     */
    public double getLocalOptimalSolution() {
        double remainder = getLastTurn()
                .getSearchPath()
                .getLastPoint()
                .distance(getLastTurn().getTreasureLocation());
        return getTraceLength() + remainder;
    }

    private Turn getLastTurn() {
        return turns.get(turns.size() - 1);
    }

    /**
     * @return The {@link Point} where the {@link com.treasure.hunt.strategy.searcher.Searcher} stood initially.
     */
    public Point getGlobalStartPoint() {
        return turns.get(0).getSearchPath().getFirstPoint();
    }

    /**
     * @return the {@link Point}, where the treasure is located.
     */
    public Point getTreasureLocation() {
        return getLastTurn().getTreasureLocation();
    }

    /**
     * @return the distance of the beeline between the {@link com.treasure.hunt.strategy.searcher.Searcher}'s initial position and the treasure location.
     */
    public double getGlobalOptimalSolution() {
        return getGlobalStartPoint().distance(getTreasureLocation());
    }

    /**
     * Calculate the solution quotient.
     * This is the quotient between the local and the global trace length optimum.
     *
     * @return solution quotient
     */
    public double getSolutionQuotient() {
        double value = getLocalOptimalSolution() / getGlobalOptimalSolution();
        if (Double.isNaN(value)) {
            return 0;
        }
        return value;
    }

    /**
     * @return The number of {@link com.treasure.hunt.strategy.hint.Hint}'s, the {@link com.treasure.hunt.strategy.searcher.Searcher} got yet.
     */
    public int getHintRequests() {
        if (turns.size() > 1 && getLastTurn().getHint() == null) {
            return turns.size() - 2;
        }
        return turns.size() - 1;
    }

    /**
     * @return {@link Statistic#getHintRequests()}/{@link Statistic#getTraceLength()}, when {@link Statistic#getTraceLength()} != 0.
     * Otherwise {@code 1}.
     */
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

    /**
     * @param turns    a list, containing {@link Turn} objects.
     * @param finished telling, whether the game is finished yet.
     * @return A list, containing {@link StatisticObject}'s.
     */
    public List<StatisticObject> calculate(List<Turn> turns, boolean finished) {
        this.turns = new ArrayList<>(turns);
        if (this.turns.size() < 1) {
            return Collections.emptyList();
        }
        ArrayList<StatisticObject> statisticObjects = new ArrayList<>(Arrays.asList(
                new StatisticObject(StatisticObject.StatisticInfo.TRACE_LENGTH, getTraceLength()),
                new StatisticObject(StatisticObject.StatisticInfo.LOCAL_OPTIMUM, getLocalOptimalSolution()),
                new StatisticObject(StatisticObject.StatisticInfo.SOLUTION_QUOTIENT, getSolutionQuotient()),
                new StatisticObject(StatisticObject.StatisticInfo.HINT_REQUEST, getHintRequests()),
                new StatisticObject(StatisticObject.StatisticInfo.HINT_TRACE_LENGTH_RATION, getHintTraceLengthRatio()),
                new StatisticObject(StatisticObject.StatisticInfo.OPTIMAL_SOLUTION, getGlobalOptimalSolution()),
                new StatisticObject(StatisticObject.StatisticInfo.FINISHED_AND_FOUND, finished ? 1 : 0)
        ));
        PreferenceService.getInstance()
                .getPreferences()
                .forEach((key, value) -> statisticObjects.add(new StatisticObject(new StatisticObject.StatisticInfo(key, "Imported from preferences", Number.class), value)));
        return statisticObjects;
    }
}
