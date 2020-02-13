package com.treasure.hunt.strategy.geom;

import lombok.Getter;

public enum StatusMessageType {
    ANGLE_HINT_DEGREE("Degree of last angle", true, true, "The degree of the last given hint in radians."),
    BASIC_TRANSFORMATION("basic transformation of the current configuration", true, false,
            "The basic transformation of the current configuration"),
    HINT_STATUS("This hint is", true, false, "")
    ;

    @Getter
    private final String name;
    @Getter
    private final boolean override;
    @Getter
    /**
     * set to true if Status is a status of the hider otherwise set to false
     */
    private final boolean fromHider;
    @Getter
    private final String description;

    StatusMessageType(String name, boolean override, boolean fromHider, String description) {
        this.name = name;
        this.override = override;
        this.fromHider = fromHider;
        this.description = description;
    }
}
