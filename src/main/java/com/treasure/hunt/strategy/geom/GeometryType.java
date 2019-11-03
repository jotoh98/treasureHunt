package com.treasure.hunt.strategy.geom;

import lombok.Getter;

public enum GeometryType {
    // hints
    FALSE_HINT(false, "False Hint"),
    TRUE_HINT(false, "True Hint"),

    // treasure/no-treasure (areas)
    NO_TREASURE(false, "no treasure"),
    POSSIBLE_TREASURE(false, "possible treasure"),

    // searcher movements
    SEARCHER_POS(true, "searcher pos", true),
    SEARCHER_MOVE(true, "no treasure"),

    // treasure location
    TREASURE(false, "no treasure"),

    // Obstacle add-on
    OBSTACLE(false, "no treasure"),
    WAY_POINT(false, "no treasure")

    // TODO add more..
    ;

    @Getter
    private final boolean enabledByDefault;
    @Getter
    private final String displayName;
    /**
     * If a new {@link GeometryItem} is passed, old one with the same type are removed.
     */
    @Getter
    private final boolean override;


    GeometryType(boolean enabledByDefault, String displayName) {
        this(enabledByDefault, displayName, false);
    }

    GeometryType(boolean enabledByDefault, String displayName, boolean override) {
        this.enabledByDefault = enabledByDefault;
        this.displayName = displayName;
        this.override = override;
    }
}