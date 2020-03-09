package com.treasure.hunt.utils;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
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
}
