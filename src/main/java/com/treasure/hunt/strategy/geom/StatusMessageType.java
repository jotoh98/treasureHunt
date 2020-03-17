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
    PREFERRED_SIZE_OF_HINT("the size of an AngleHint", true, true, "the size of the hint in radians."), // maybe to preferences
    REMAINING_POSSIBLE_AREA("the remaining area in which the treasure could be", true, true, "The area which has not been excluded by previous hints"),
    RELATIVE_AREA_CUTOFF("relative area cutoff" , true, true, "the area which has been excluded by this area in percent"),
    DISTANCE_TREASURE_TO_CENTROID(" the distance beetween Centroid and the tresure", true, true, "the distance of the remaining possible area's centroid and the treasure"),
    DISTANCE_ANGLE_BISECTOR_TREASURE("distance from Normal AngleLine to treasure" , true, true, "distance from AngleHint bisector to treasure");

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
