package com.treasure.hunt.strategy.geom;

import lombok.Getter;

/**
 * This is conventions for GeometryItems,
 * how to display them.
 *
 * @author jotoh
 */
public enum GeometryType {
    // hints
    FALSE_HINT(false, "False Hint"),
    HINT_CENTER(false, "hint-center"),
    HINT_RADIUS(false, "hint-center"),
    TRUE_HINT(false, "True Hint"),

    // treasure/no-treasure (areas)
    NO_TREASURE(false, "no treasure"),
    POSSIBLE_TREASURE(false, "possible treasure"),
    HINT_ANGLE(true, "angle hint", true),

    // searcher movements
    SEARCHER_POSITION(true, "searcher position"),
    SEARCHER_MOVEMENT(true, "searcher movement"),

    // treasure location
    TREASURE(true, "no treasure"),

    // Obstacle add-on
    OBSTACLE(false, "no treasure"),
    WAY_POINT(true, "no treasure"),

    STANDARD(true, ""),

    BOUNDING_CIRCE(false,"bounding circle"),

    // StrategyFromPaper2
    WHITE_TILE(true, "white tile"),
    BLACK_TILE(true, "black tile"),
    RECTANGLE_SCAN_MOVEMENT(true, "rectangle scan movement"),

    // IntelligentAngleSearcher
    SEARCH_BOUNDING(true, "Search-Bounding"),
    SEARCH_RECTANGLE_BOUNDING(true, "Search-Rectangle")

    // TODO add more..
    ;

    @Getter
    private final String displayName;
    @Getter
    private boolean enabled;
    @Getter
    private boolean override;

    GeometryType(boolean enabled, String displayName, boolean override) {
        this.displayName = displayName;
        this.enabled = enabled;
        this.override = override;
    }

    GeometryType(boolean enabledByDefault, String displayName) {
        this(enabledByDefault, displayName, false);
    }

}