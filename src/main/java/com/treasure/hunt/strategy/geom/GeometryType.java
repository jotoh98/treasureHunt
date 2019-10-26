package com.treasure.hunt.strategy.geom;

public enum GeometryType {
    // hints
    FALSE_HINT,
    TRUE_HINT,

    // treasure/no-treasure (areas)
    NO_TREASURE,
    POSSIBLE_TREASURE,

    // seeker movements
    SEEKER_POS,
    SEEKER_MOVE,

    // treasure location
    TREASURE,

    // Obstacle add-on
    OBSTACLE,

    // TODO add more..
    ;
}