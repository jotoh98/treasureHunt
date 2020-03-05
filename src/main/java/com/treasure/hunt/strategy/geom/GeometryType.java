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

    // HalfPlaneHint
    HALF_PLANE(true, "the treasure is not here", true),
    HALF_PLANE_LINE(true, "half plane line", true),
    HALF_PLANE_LINE_BLUE(true, "half plane line", true),
    HALF_PLANE_LINE_BROWN(true, "half plane line", true),

    // treasure/no-treasure (areas)
    TREASURE(true, "treasure"),
    TREASURE_FLAG(true, "treasure flag", true),
    NO_TREASURE(false, "no treasure"),
    POSSIBLE_TREASURE(false, "possible treasure"),

    // searcher movements
    WAY_POINT(true, "no treasure"),
    SEARCHER_MOVEMENT(true, "searcher movement"),
    SEARCHER_LAST_MOVE(true, "the searchers last movement", true),

    BOUNDING_CIRCE(false, "bounding circle"),

    // StrategyFromPaper
    CURRENT_PHASE(false, "current phase", true),
    CURRENT_RECTANGLE(true, "current rectangle", true),
    CURRENT_POLYGON(true, "current polygon", true),

    STANDARD(true, ""),
    CURRENT_WAY_POINT(true, "Current way point", true),
    GRID(true, "Grid", false);

    @Getter
    private final String displayName;
    @Getter
    private boolean enabled;
    @Getter
    private boolean override;
    @Getter
    private boolean multiStyle = false;

    GeometryType(boolean enabled, String displayName, boolean override) {
        this.displayName = displayName;
        this.enabled = enabled;
        this.override = override;
    }

    GeometryType(boolean enabled, String displayName, boolean override, boolean multiStyle) {
        this(enabled, displayName, override);
        this.multiStyle = multiStyle;
    }

    GeometryType(boolean enabledByDefault, String displayName) {
        this(enabledByDefault, displayName, false);
    }

}