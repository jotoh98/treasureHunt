package com.treasure.hunt.strategy.geom;

import lombok.Value;

@Value
public class StatusMessageItem {
    StatusMessageType statusMessageType;
    String message;

    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
