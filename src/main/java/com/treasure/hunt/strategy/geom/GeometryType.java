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

    public static GeometryType STANDARD() {
        return new GeometryType(true, Color.black, "");
    }

}
