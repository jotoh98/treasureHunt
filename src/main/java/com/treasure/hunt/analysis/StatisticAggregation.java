package com.treasure.hunt.analysis;

import com.treasure.hunt.utils.ListUtils;
import lombok.Getter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Getter
public enum StatisticAggregation {
    MIN(numbers -> Collections.min(numbers, Comparator.comparing(Number::doubleValue))),
    MAX(numbers -> Collections.max(numbers, Comparator.comparing(Number::doubleValue))),
    AVERAGE(numbers -> numbers.stream().mapToDouble(Number::doubleValue).average().getAsDouble()),
    DEVIATION(ListUtils::standardDeviation);

    private final Function<List<Number>, Number> aggregation;

    StatisticAggregation(Function<List<Number>, Number> aggregation) {
        this.aggregation = aggregation;

    }
}
