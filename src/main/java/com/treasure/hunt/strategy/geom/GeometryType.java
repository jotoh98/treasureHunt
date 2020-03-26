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

    // HalfPlaneHint
    HALF_PLANE(true, "the treasure is not here", true),
    HALF_PLANE_ORANGE(false, "half plane line", true), // todo add better description
    HALF_PLANE_RED(true, "half plane line", true),// todo add better description
    HALF_PLANE_LIGHT_RED(true, "half plane line", true),// todo add better description

    // treasure/no-treasure (areas)
    TREASURE(true, "treasure", true),
    TREASURE_FLAG(true, "treasure flag", true),
    NO_TREASURE(false, "no treasure"),
    POSSIBLE_TREASURE(false, "possible treasure"),
    HINT_ANGLE(true, "angle hint", true),

    // searcher movements
    WAY_POINT(true, "way point"),
    WAY_POINT_LINE(true, "way point line"),
    SEARCHER_MOVEMENT(true, "searcher movement"),
    SEARCHER_LAST_MOVE(true, "the searchers last movement", true),

    BOUNDING_CIRCE(false, "bounding circle"),

    // StrategyFromPaper
    CURRENT_PHASE(false, "current phase", true),
    CURRENT_RECTANGLE(true, "current rectangle", true),
    CURRENT_POLYGON(true, "current polygon", true),
    L1_DOUBLE_APOS(true, "L1''", true),

    STANDARD(true, ""),
    CURRENT_WAY_POINT(true, "Current way point", true),
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