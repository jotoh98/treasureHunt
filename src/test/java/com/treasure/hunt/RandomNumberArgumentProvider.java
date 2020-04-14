package com.treasure.hunt;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Stream;

/**
 * @author Ben Rank
 */
public class RandomNumberArgumentProvider implements ArgumentsProvider {
    private static final int NUMBER = 300;
    private final Random random = new Random(213645677823434L);

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        ArrayList<Arguments> arguments = new ArrayList<>();
        for (int i = 0; i < NUMBER; i++) {
            arguments.add(Arguments.of(random.nextInt()));
        }
        return arguments.stream();
    }
}
