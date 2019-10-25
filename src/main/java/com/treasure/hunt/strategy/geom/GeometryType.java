package com.treasure.hunt.strategy.geom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeometryType {
    boolean visibleByDefault = true;
    Color lineColor = Color.black;
    Color fillColor = Color.black;
    boolean filled = false;
    String displayText = "";


    public GeometryType(boolean visibleByDefault, Color lineColor, String displayText) {
        this(visibleByDefault, lineColor, Color.black, false, displayText);
    }

    public GeometryType(boolean visibleByDefault, Color lineColor) {
        this(visibleByDefault, lineColor, "");
    }

    public GeometryType(boolean visibleByDefault, Color lineColor, Color fillColor) {
        this(visibleByDefault, lineColor, fillColor, true, "");
    }

}

/*public enum GeometryType {
    WAY_POINT,       // This describes a point showing, where the player stands
    TREASURE_LOCATION,  // This describes a point showing, where the treasure actually lies.
    MOVES,          // This describes a geometry object showing, how the player has moves
    TREASURE_GUESS, // This describes a geometry object showing, where the player guesses the treasure
    SAFE_AREA,      // This describes a geometry object showing, where the treasure is NOT.
    HINT,           // This describes a gemoetry objects showing, where the treasure is.
    // TODO add more ..
}*/