package com.treasure.hunt.strategy.hint;

import com.treasure.hunt.strategy.Generator;
import org.locationtech.jts.geom.Point;

public abstract class AbstractHintGenerator<T extends Hint> extends Generator {
    protected double insecurity;

    protected abstract HintAndTarget<T> generate(Point currentLocationOfAgent);

    public void init(double insecurity) {
        this.insecurity = insecurity;
    }

    public HintAndTarget<T> generateHint(Point currentLocationOfAgent) {
        return generate(currentLocationOfAgent);
    }


}