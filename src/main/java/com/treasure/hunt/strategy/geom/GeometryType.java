package com.treasure.hunt.strategy.geom;

import lombok.Getter;

/**
 * This is conventions for GeometryItems,
 * how to display them.
 *
 * @author jotoh
 */
public enum GeometryType {
    /**
     * Hint relevant
     */
    HINT_CENTER(false, "hint-center"),

    /**
     * HalfPlaneHint relevant
     */
    HALF_PLANE(true, "the treasure is not here", true),
    HALF_PLANE_CURRENT_RED(false, "current half plane hint", true),
    HALF_PLANE_PREVIOUS_LIGHT_RED(true, "previous half plane hint", true),
    HALF_PLANE_BEFORE_PREVIOUS_ORANGE(true, "the half plane hint received before the previous half plane hint", true),

    /**
     * treasure/no-treasure (areas) relevant
     */
    TREASURE(true, "treasure", true),
    TREASURE_FLAG(true, "treasure flag", true),
    NO_TREASURE(false, "no treasure", true),
    POSSIBLE_TREASURE(false, "possible treasure", true),
    HINT_ANGLE(true, "angle hint", true),

    /**
     * searcher movements relevant
     */
    WAY_POINT(true, "way point"),
    WAY_POINT_LINE(true, "way point line"),
    CURRENT_WAY_POINT(true, "Current way point", true),

    HELPER_LINE(true, "helper line"),

    BOUNDING_CIRCE(false, "bounding circle", true),
    OUTER_CIRCLE(false, "outer circle", true),
    WORST_CONSTANT(false, "Point with worst Constant", true),
    INNER_BUFFER(true, "inner buffer area", true),
    CENTROID(true, "Centroid of remaining possible Area", true),

    /**
     * StrategyFromPaper relevant
     */
    CURRENT_PHASE(false, "current phase", true),
    CURRENT_RECTANGLE(true, "current rectangle", true),
    CURRENT_POLYGON(true, "current polygon", true),
    L1_DOUBLE_APOS(true, "L1''", true),

    // XY Searcher
    MAX_X(true, "max x", true),
    MAX_Y(true, "max x", true),
    MIN_X(true, "max x", true),
    MIN_Y(true, "max x", true),

    STANDARD(true, ""),
    GRID(true, "Grid", false),
    HIGHLIGHTER(true, "Highlighter", true),
    POLYHEDRON(true, "Polyhedron", true);

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
