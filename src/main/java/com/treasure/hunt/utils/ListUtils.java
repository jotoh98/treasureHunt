package com.treasure.hunt.utils;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ListUtils {
    /**
     * Map a list to a stream using two consecutive values of the list.
     *
     * @param list   list to map
     * @param mapper mapper function to map two consecutive values to a new value
     * @param <T>    input type
     * @param <R>    output type
     * @return stream of consecutive reduced values
     */
    public static <T, R> Stream<R> consecutive(List<T> list, BiFunction<T, T, R> mapper) {
        return IntStream.range(1, list.size())
                .boxed()
                .map(i -> mapper.apply(list.get(i - 1), list.get(i)));
    }

    public static <T, R> Stream<R> consecutive(T[] list, BiFunction<T, T, R> mapper) {
        return IntStream.range(1, list.length)
                .boxed()
                .map(i -> mapper.apply(list[i - 1], list[i]));
    }
}
