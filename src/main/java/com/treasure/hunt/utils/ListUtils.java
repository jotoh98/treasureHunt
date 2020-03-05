package com.treasure.hunt.utils;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ListUtils {
    public static <T, R> Stream<R> consecutive(List<T> list, BiFunction<T, T, R> mapper) {
        return IntStream.range(1, list.size()).boxed()
                .map(i -> mapper.apply(list.get(i - 1), list.get(i)));
    }
}
