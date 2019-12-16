package com.treasure.hunt.strategy.geom;

/**
 * This is conventions for GeometryItems,
 * how to display them.
 *
 * @author hassel
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
    HINT_ANGLE(true, "angle hint"),

    // searcher movements
    SEARCHER_POSITION(true, "searcher position"),
    SEARCHER_MOVEMENT(true, "searcher movement"),

    // treasure location
    TREASURE(true, "no treasure"),

    // Obstacle add-on
    OBSTACLE(false, "no treasure"),
    WAY_POINT(true, "no treasure"),

    STANDARD(true, ""),

    BOUNDING_CIRCE(false,"bounding circle")
    // TODO add more..
    ;

    @Getter
    private final String displayName;
    private boolean enabled;
    private boolean override;

    GeometryType(String displayName, boolean enabled, boolean override) {
        this.displayName = displayName;
        this.enabled = enabled;
        this.override = override;
    }

    GeometryType(boolean enabledByDefault, String displayName) {
        this(displayName, enabledByDefault, false);
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isOverride() {
        return this.override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }
}