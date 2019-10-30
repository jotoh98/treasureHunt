package com.treasure.hunt.strategy.geom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GeometryStyle {

    private boolean visible;

    private boolean filled;

    private Color outlineColor;

    private Color fillColor;

    public GeometryStyle(boolean visible, Color outlineColor) {
        this(visible, false, outlineColor, Color.black);
    }

    public GeometryStyle(boolean visible, Color outlineColor, Color fillColor) {
        this(visible, true, outlineColor, fillColor);
    }

    public static GeometryStyle getDefaults(GeometryType type) {
        switch (type) {
            case WAY_POINT:
            case SEEKER_POS:
                return new GeometryStyle(true, Color.black);
            case POSSIBLE_TREASURE:
                return new GeometryStyle(true, Color.gray, Color.lightGray);
        }
        return new GeometryStyle(true, Color.black);
    }

}
