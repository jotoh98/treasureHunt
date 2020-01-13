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
    HINT_ANGLE(true, "angle hint", true),
    HINT_CENTER(false, "hint-center"),

    // treasure/no-treasure (areas)
    TREASURE(true, "no treasure"),
    NO_TREASURE(false, "no treasure"),
    POSSIBLE_TREASURE(false, "possible treasure"),

    // searcher movements
    WAY_POINT(true, "no treasure"),
    SEARCHER_MOVEMENT(true, "searcher movement"),

    BOUNDING_CIRCE(false, "bounding circle"),

    HIGHLIGHTER(true, "highlighter"),

    STANDARD(true, "")
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