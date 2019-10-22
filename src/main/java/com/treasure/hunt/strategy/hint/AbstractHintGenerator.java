package com.treasure.hunt.strategy.hint;

import com.treasure.hunt.strategy.Generator;
import com.treasure.hunt.strategy.moves.Moves;

public abstract class AbstractHintGenerator<T extends Hint> extends Generator {
    private double insecurity;

    protected abstract T generate(Moves moves);

    public void init(double insecurity) {
        this.insecurity = insecurity;
    }

    public T generateHint(Moves moves) {
        return generate(moves);
    }


}