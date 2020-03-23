package com.treasure.hunt.analysis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.util.Arrays;
import java.util.List;

/**
 * This defines a component, we want to analyze in our simulation.
 *
 * @author Trostorff, Daniel
 */
@Value
public class StatisticObject {
    StatisticInfo statisticInfo;
    Number value;

    @EqualsAndHashCode(of = "name")
    public static class StatisticInfo {
        public static final StatisticInfo TRACE_LENGTH = new StatisticInfo("Trace length",
                "If finished: Length of searchers path; if unfinished: Length of searchers path plus the direct route from searchers last point to treasure",
                Double.class);
        public static final StatisticInfo SOLUTION_QUOTIENT = new StatisticInfo("Solution quotient",
                "The Quotient of the optimum solution and the trace length",
                Double.class);
        public static final StatisticInfo HINT_TRACE_LENGTH_RATION = new StatisticInfo("Hint-trace-length-ratio",
                "The quotient of hint requests and trace length",
                Double.class);
        public static final StatisticInfo HINT_REQUEST = new StatisticInfo("Hint-requests",
                "Number of requested hints",
                Double.class);
        public static final StatisticInfo OPTIMAL_SOLUTION = new StatisticInfo("Optimal solution",
                "The euclidean distance between treasure and searchers start position.",
                Double.class);
        @Getter
        private final String name;
        @Getter
        private final String description;
        @Getter
        private final Class type;

        StatisticInfo(String name, String description, Class type) {
            this.name = name;
            this.description = description;
            this.type = type;
        }

        public static List<StatisticInfo> getAllStatisticInfo() {
            return Arrays.asList(TRACE_LENGTH, SOLUTION_QUOTIENT, HINT_TRACE_LENGTH_RATION, HINT_REQUEST, OPTIMAL_SOLUTION);
        }
    }
}
