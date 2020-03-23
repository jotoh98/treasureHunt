package com.treasure.hunt.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ListUtils {
    public static <T, R> Stream<R> consecutive(List<T> list, BiFunction<T, T, R> mapper) {
        return IntStream.range(1, list.size()).boxed()
                .map(i -> mapper.apply(list.get(i - 1), list.get(i)));
    }

    /**
     * Apply some stream filters to a stream.
     *
     * @param input   stream to be filtered
     * @param filters multiple filters to be applied
     * @return filtered input
     */
    @SafeVarargs
    public static <T> Stream<T> filterStream(Stream<T> input, Function<Stream<T>, Stream<T>>... filters) {
        return Arrays.stream(filters)
                .reduce(Function::andThen)
                .orElse(Function.identity())
                .apply(input);
    }

    public static <T> List<List<T>> transpose(List<List<T>> table) {
        List<List<T>> ret = new ArrayList<List<T>>();
        final int N = table.get(0).size();
        for (int i = 0; i < N; i++) {
            List<T> col = new ArrayList<T>();
            for (List<T> row : table) {
                col.add(row.get(i));
            }
            ret.add(col);
        }
        return ret;
    }

    public static <T> boolean anyMatch(T[] list, Predicate<T> predicate) {
        for (T item : list) {
            if (predicate.test(item)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean allMatch(T[] list, Predicate<T> predicate) {
        for (T item : list) {
            if (!predicate.test(item)) {
                return false;
            }
        }
        return true;
    }

    public static <T> boolean noneMatch(T[] list, Predicate<T> predicate) {
        return allMatch(list, predicate.negate());
    }
  
    public static double standardDeviation(List<Number> table) {
        // Step 1:
        double mean = table.stream().mapToDouble(Number::doubleValue).average().getAsDouble();
        double temp = 0;

        for (int i = 0; i < table.size(); i++) {
            double val = table.get(i).doubleValue();

            // Step 2:
            double squrDiffToMean = Math.pow(val - mean, 2);

            // Step 3:
            temp += squrDiffToMean;
        }

        // Step 4:
        double meanOfDiffs = temp / (double) (table.size());

        // Step 5:
        return Math.sqrt(meanOfDiffs);
    }
}
