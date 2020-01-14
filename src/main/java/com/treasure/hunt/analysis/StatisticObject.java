package com.treasure.hunt.analysis;

import lombok.Getter;
import lombok.Value;

/**
 * This defines a component, we want to analyze in our simulation.
 *
 * @author Trostorff, Daniel
 */
@Value
public class StatisticObject {
    StatisticInfo statisticInfo;
    Object value;

    public enum StatisticInfo {
        TRACE_LENGTH("Trace length",
                "If finished: Length of searchers path; if unfinished: Length of searchers path plus the direct route from searchers last point to treasure",
                Double.class),
        SOLUTION_QUOTIENT("Solution quotient",
                "The Quotient of the optimum solution and the trace length",
                Double.class),
        HINT_TRACE_LENGTH_RATION("Hint-trace-length-ratio",
                "The quotient of hint requests and trace length",
                Double.class),
        HINT_REQUEST("Hint-requests",
                "Number of requested hints",
                Double.class),
        OPTIMAL_SOLUTION("Optimal solution",
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
    }
}
