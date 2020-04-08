package com.treasure.hunt.analysis;

import com.treasure.hunt.game.Turn;
import com.treasure.hunt.service.preferences.PreferenceService;
import com.treasure.hunt.strategy.searcher.SearchPath;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class to calculate various statistics.
 *
 * @author Trostorff, Daniel
 */
public class Statistic {
    private List<Turn> lastTurn;
    private Double lastMaxSolutionQuotient;

    /**
     * @return The length of the trace, the {@link com.treasure.hunt.strategy.searcher.Searcher} ran yet.
     */
    public double getTraceLength(final List<Turn> turns) {
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
    public double getLocalOptimalSolution(final List<Turn> turns) {
        double remainder = getLastTurn(turns)
                .getSearchPath()
                .getLastPoint()
                .distance(getLastTurn(turns).getTreasureLocation());
        return getTraceLength(turns) + remainder;
    }

    private Turn getLastTurn(final List<Turn> turns) {
        return turns.get(turns.size() - 1);
    }

    /**
     * @return The {@link Point} where the {@link com.treasure.hunt.strategy.searcher.Searcher} stood initially.
     */
    public Point getGlobalStartPoint(final List<Turn> turns) {
        return turns.get(0).getSearchPath().getFirstPoint();
    }

    /**
     * @return the {@link Point}, where the treasure is located.
     */
    public Point getTreasureLocation(final List<Turn> turns) {
        return getLastTurn(turns).getTreasureLocation();
    }

    /**
     * @return the distance of the beeline between the {@link com.treasure.hunt.strategy.searcher.Searcher}'s initial position and the treasure location.
     */
    public double getGlobalOptimalSolution(final List<Turn> turns) {
        return getGlobalStartPoint(turns).distance(getTreasureLocation(turns));
    }

    /**
     * Calculate the solution quotient.
     * This is the quotient between the local and the global trace length optimum.
     *
     * @return solution quotient
     */
    public double getSolutionQuotient(final List<Turn> turns) {
        return getLocalOptimalSolution(turns) / getGlobalOptimalSolution(turns);
    }

    /**
     * Calculate the solution quotient.
     * This is the quotient between the local and the global trace length optimum.
     *
     * @return solution quotient
     */
    public double getMaxSolutionQuotientOverTime(final List<Turn> turns) {
        double maxSolutionQuotient = 0;
        if (lastTurn != null && lastTurn.size() == turns.size() - 1) {
            maxSolutionQuotient = Math.max(lastMaxSolutionQuotient, getSolutionQuotient(turns));

        } else {
            for (int i = 0; i < turns.size(); i++) {
                maxSolutionQuotient = Math.max(maxSolutionQuotient, getSolutionQuotient(turns.subList(0, i + 1)));
            }
        }
        lastMaxSolutionQuotient = maxSolutionQuotient;
        return maxSolutionQuotient;
    }

    /**
     * @return The number of {@link com.treasure.hunt.strategy.hint.Hint}'s, the {@link com.treasure.hunt.strategy.searcher.Searcher} got yet.
     */
    public int getHintRequests(final List<Turn> turns) {
        if (turns.size() > 1 && getLastTurn(turns).getHint() == null) {
            return turns.size() - 2;
        }
        return turns.size() - 1;
    }

    /**
     * @return {@link Statistic#getHintRequests(List)}/{@link Statistic#getTraceLength(List)}, when {@link Statistic#getTraceLength(List)} != 0.
     * Otherwise {@code 1}.
     */
    public double getHintTraceLengthRatio(final List<Turn> turns) {
        final double traceLength = getTraceLength(turns);
        if (traceLength == 0) {
            return 1;
        }
        return getHintRequests(turns) / traceLength;
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
        ArrayList<StatisticObject> statisticObjects = new ArrayList<>(Arrays.asList(
                new StatisticObject(StatisticObject.StatisticInfo.TRACE_LENGTH, getTraceLength(turns)),
                new StatisticObject(StatisticObject.StatisticInfo.LOCAL_OPTIMUM, getLocalOptimalSolution(turns)),
                new StatisticObject(StatisticObject.StatisticInfo.SOLUTION_QUOTIENT, getSolutionQuotient(turns)),
                new StatisticObject(StatisticObject.StatisticInfo.HINT_REQUEST, getHintRequests(turns)),
                new StatisticObject(StatisticObject.StatisticInfo.HINT_TRACE_LENGTH_RATION, getHintTraceLengthRatio(turns)),
                new StatisticObject(StatisticObject.StatisticInfo.OPTIMAL_SOLUTION, getGlobalOptimalSolution(turns)),
                new StatisticObject(StatisticObject.StatisticInfo.MAX_SOLUTION_QUOTIENT_OVER_TIME, getMaxSolutionQuotientOverTime(turns)),
                new StatisticObject(StatisticObject.StatisticInfo.FINISHED_AND_FOUND, finished ? 1 : 0)
        ));
        this.lastTurn = new ArrayList<>(turns);
        PreferenceService.getInstance()
                .getPreferences()
                .forEach((key, value) -> statisticObjects.add(new StatisticObject(new StatisticObject.StatisticInfo(key, "Imported from preferences", Number.class), value)));
        return statisticObjects;
    }
}
