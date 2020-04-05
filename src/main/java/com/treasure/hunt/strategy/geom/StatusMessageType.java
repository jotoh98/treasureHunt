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
    ROTATION_RECTANGLE("The angle the rectangles are rotated by (in radians)", true, false,
            "The rotation of the phase and current rectangle (in radians)"),
    // hider:
    EXPLANATION_VISUALISATION_HIDER("An explanation for the visualisation used by the hider", true,
            true, "An explanation for the visualisation used by the hider"),
    PREFERRED_SIZE_OF_HINT("the size of an AngleHint", true, true, "the size of the hint in radians."), // maybe to preferences
    REMAINING_POSSIBLE_AREA("the remaining area in which the treasure could be", true, true, "The area which has not been excluded by previous hints"),

    RELATIVE_AREA_CUTOFF("relative area cutoff", true, true, "the area which has been excluded by this area in percent"),
    RELATIVE_AREA_CUTOFF_RATING("rating: relative area cutoff", true, true, "the rating the relative area adds to the overall hint rating"),

    DISTANCE_TREASURE_TO_CENTROID("distance Centroid-Treasure", true, true, "the distance of the remaining possible area's centroid and the treasure"),
    DISTANCE_TREASURE_TO_CENTROID_RATING("rating: distance Centroid-Treasure", true, true, "the rating for distance of the remaining possible area's centroid and the treasure"),

    DISTANCE_ANGLE_BISECTOR_TREASURE("distance from angle bisector to treasure", true, true, "distance from AngleHint bisector to treasure"),
    DISTANCE_ANGLE_BISECTOR_RATING("rating : distance from Normal bisector to treasure ", true, true, "the rating for the distance from bisector to treasure"),

    HINT_QUALITY_HIDER("hintquality strategyFromPaper", true, true, "the hintquality in terms of currentRectangle from StrategyFromPaper"),
    HINT_QUALITY_HIDER_RATING("rating: hintquality strategyFromPaper", true, true, "the rating the hintquality adds"),

    OVERALL_HINT_RATING("rating: of the current hint", true, true, "the rating the given hint has received"),
    CURRENT_TREASURE_POSITION("current treasure position", true, true, "the current x and y values of the treasure");

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
