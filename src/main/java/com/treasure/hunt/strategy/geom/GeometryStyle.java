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
            case CURRENT_PHASE:
                return new GeometryStyle(true, Color.YELLOW);
            case CURRENT_RECTANGLE:
                return new GeometryStyle(true, Color.RED);
            case SEARCHER_LAST_MOVE:
                return new GeometryStyle(true, new Color(0x007A1D));
            case HALF_PLANE:
                return new GeometryStyle(true, new Color(0x505050), new Color(0x505050));
            case HALF_PLANE_LINE:
                return new GeometryStyle(true, new Color(0x000000));
            case HALF_PLANE_LINE_BLUE:
                return new GeometryStyle(true, Color.BLUE);
        }
        return new GeometryStyle(true, Color.lightGray);
    }

    public Stroke getStroke() {
        return new BasicStroke(1);
    }

}
