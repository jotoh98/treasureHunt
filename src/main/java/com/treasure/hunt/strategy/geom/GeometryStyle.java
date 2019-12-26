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
            case WHITE_TILE:
                return new GeometryStyle(true, new Color(0,255, 0, 100), new Color(0,255, 0, 0));
            case BLACK_TILE:
                return new GeometryStyle(true,new Color(255,0, 0, 200), new Color(255,0, 0, 100));
            case RECTANGLE_SCAN_MOVEMENT:
                return new GeometryStyle(true, new Color(0x0000FF));
            case SEARCH_BOUNDING:
                return new GeometryStyle(true, new Color(0,255, 0, 255));
            case SEARCH_RECTANGLE_BOUNDING:
                return new GeometryStyle(true, new Color(255, 0, 0, 150));
        }
        return new GeometryStyle(true, Color.lightGray);
    }

    public Stroke getStroke() {
        return new BasicStroke(1);
    }

}
