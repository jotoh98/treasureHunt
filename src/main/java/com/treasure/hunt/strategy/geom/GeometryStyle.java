package com.treasure.hunt.strategy.geom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jotoh
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GeometryStyle {

    public static EnumMap<GeometryType, GeometryStyle> defaultsMap = new EnumMap<>(GeometryType.class);

    static {
        HashMap defaultsMapAdd = new HashMap(Map.of(
                GeometryType.WAY_POINT, new GeometryStyle(true, new Color(0xFFFFFF), 1),
                GeometryType.TREASURE, new GeometryStyle(true, new Color(0xFFD700), 2),
                GeometryType.HINT_ANGLE, new GeometryStyle(true, new Color(0x575757)),
                GeometryType.GRID, new GeometryStyle(true, false, new Color(0x555555), null, -1),
                GeometryType.SEARCHER_LAST_MOVE, new GeometryStyle(true, new Color(0x007A1D)),
                GeometryType.CURRENT_WAY_POINT, new GeometryStyle(true, new Color(0x007A1D)),
                GeometryType.HALF_PLANE, new GeometryStyle(true, new Color(0x777777), new Color(0x22777777, true)),
                GeometryType.HALF_PLANE_CURRENT_RED, new GeometryStyle(true, new Color(0x33ff0000, true), new Color(0x33ff0000, true)),
                GeometryType.HALF_PLANE_PREVIOUS_LIGHT_RED, new GeometryStyle(true, new Color(0x18ff0000, true), new Color(0x18ff0000, true))
        ));
        defaultsMapAdd.put(GeometryType.CURRENT_POLYGON, new GeometryStyle(true, new Color(0x40E0D0)));
        defaultsMapAdd.put(GeometryType.HALF_PLANE_BEFORE_PREVIOUS_ORANGE, new GeometryStyle(true, new Color(0x11ff9933, true), new Color(0x11ff9933, true)));
        defaultsMapAdd.put(GeometryType.WAY_POINT_LINE, new GeometryStyle(true, new Color(0xFFFFFF)));
        defaultsMapAdd.put(GeometryType.HIGHLIGHTER, new GeometryStyle(true, Color.GREEN, Integer.MAX_VALUE));
        defaultsMapAdd.put(GeometryType.CURRENT_PHASE, new GeometryStyle(true, new Color(0x008504)));
        defaultsMapAdd.put(GeometryType.CURRENT_RECTANGLE, new GeometryStyle(true, new Color(0x00e007)));
        defaultsMapAdd.put(GeometryType.HELPER_LINE, new GeometryStyle(true, Color.DARK_GRAY, -1));
        defaultsMap.putAll(defaultsMapAdd);
        defaultsMapAdd.put(GeometryType.MAX_X, new GeometryStyle(true, new Color(0x000000)));
        defaultsMapAdd.put(GeometryType.MAX_Y, new GeometryStyle(true, new Color(0x000000)));
        defaultsMapAdd.put(GeometryType.MIN_X, new GeometryStyle(true, new Color(0x000000)));
        defaultsMapAdd.put(GeometryType.MIN_Y, new GeometryStyle(true, new Color(0x000000)));
        defaultsMapAdd.put(GeometryType.L1_DOUBLE_APOS, new GeometryStyle(true, Color.BLUE));
        defaultsMap.putAll(defaultsMapAdd);
    }

    private boolean visible;

    private boolean filled;

    private Color outlineColor;

    private Color fillColor;

    private int zIndex = 0;

    public GeometryStyle(boolean visible, Color outlineColor) {
        this(visible, false, outlineColor, Color.black, 0);
    }

    public GeometryStyle(boolean visible, Color outlineColor, Color fillColor) {
        this(visible, true, outlineColor, fillColor, 0);
    }

    public GeometryStyle(boolean visible, Color outlineColor, int zIndex) {
        this.visible = visible;
        this.outlineColor = outlineColor;
        this.zIndex = zIndex;
    }

    public static GeometryStyle getDefaults(GeometryType type) {

        if (defaultsMap.containsKey(type)) {
            return defaultsMap.get(type).clone();
        }
        return new GeometryStyle(true, Color.lightGray);
    }

    public static void registerDefault(GeometryType type, GeometryStyle style) {
        defaultsMap.put(type, style);
    }

    public Stroke getStroke() {
        return new BasicStroke(1);
    }

    protected GeometryStyle clone() {
        return new GeometryStyle(visible, filled, outlineColor, fillColor, zIndex);
    }
}
