package com.treasure.hunt.strategy.geom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;

/**
 * @author jotoh
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GeometryStyle {

    private boolean visible;

    private boolean filled;

    private Color outlineColor;

    private Color fillColor;

    int zIndex = 0;

    public GeometryStyle(boolean visible, Color outlineColor) {
        this(visible, false, outlineColor, Color.black, 0);
    }

    public GeometryStyle(boolean visible, Color outlineColor, Color fillColor) {
        this(visible, true, outlineColor, fillColor, 0);
    }

    public static GeometryStyle getDefaults(GeometryType type) {
        switch (type) {
            case WAY_POINT:
                return new GeometryStyle(true, new Color(0xFFFFFF));
            case TREASURE:
                return new GeometryStyle(true, new Color(0xFFD700));
            case HINT_ANGLE:
                return new GeometryStyle(true, new Color(0x575757));
        }
        return new GeometryStyle(true, Color.lightGray);
    }

    public Stroke getStroke() {
        return new BasicStroke(1);
    }

}
