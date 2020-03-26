package com.treasure.hunt.strategy.geom;

import lombok.Getter;

public enum StatusMessageType {
    ANGLE_HINT_DEGREE("Degree of last angle", true, true, "The degree of the last given hint in radians."),
    PREVIOUS_HINT_QUALITY("The quality of the previous hint", true, false,
            "The quality of the previous hint"),
    BEFORE_PREVIOUS_QUALITY("The quality of the hint received before the previous hint", true,
            false, "The quality of the hint received before the previous hint"),
    EXPLANATION_MOVEMENT("An explanation for the movement of the searcher", true, false,
            "An explanation for the movement of the searcher"),
    EXPLANATION_STRATEGY("An explanation for the general idea of the strategy", true, false,
            "An explanation for the general idea of the strategy"),
    EXPLANATION_VISUALISATION_SEARCHER("An explanation for the visualisation used by the searcher", true,
            false, "An explanation for the visualisation used by the searcher"),
    // hider:
    EXPLANATION_VISUALISATION_HIDER("An explanation for the visualisation used by the hider", true,
            true, "An explanation for the visualisation used by the hider");

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
