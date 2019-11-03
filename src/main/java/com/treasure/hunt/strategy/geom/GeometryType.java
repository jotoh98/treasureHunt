package com.treasure.hunt.strategy.geom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum GeometryType {
    // hints
    FALSE_HINT(false, "False Hint"),
    TRUE_HINT(false, "True Hint"),

    // treasure/no-treasure (areas)
    NO_TREASURE(false, "no treasure"),
    POSSIBLE_TREASURE(false, "possible treasure"),

    // searcher movements
    SEARCHER_POS(true, "searcher pos"),
    SEARCHER_MOVE(true, "no treasure"),

    // treasure location
    TREASURE(false, "no treasure"),

    // Obstacle add-on
    OBSTACLE(false, "no treasure"),
    WAY_POINT(false, "no treasure"),

    STANDARD(true, "")

    // TODO add more..
    ;

    @Getter
    @Setter
    private boolean enabled;

    @Getter
    private final String displayName;

    @Getter
    @Setter
    private boolean override;

    GeometryType(boolean enabledByDefault, String name) {
        this(enabledByDefault, name, false);
    }
}