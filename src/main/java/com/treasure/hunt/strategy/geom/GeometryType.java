package com.treasure.hunt.strategy.geom;


public enum GeometryType {
    WAY_POINT,       // This describes a point showing, where the player stands
    TREASURE_LOCATION,  // This describes a point showing, where the treasure actually lies.
    MOVES,          // This describes a geometry object showing, how the player has moves
    TREASURE_GUESS, // This describes a geometry object showing, where the player guesses the treasure
    SAFE_AREA,      // This describes a geometry object showing, where the treasure is NOT.
    HINT,           // This describes a gemoetry objects showing, where the treasure is.
    // TODO add more ..
}
