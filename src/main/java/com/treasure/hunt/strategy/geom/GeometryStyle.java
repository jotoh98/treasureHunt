package com.treasure.hunt.strategy.geom;

import java.awt.*;

/**
 * @author hassel
 */
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

    public GeometryStyle(boolean visible, boolean filled, Color outlineColor, Color fillColor) {
        this.visible = visible;
        this.filled = filled;
        this.outlineColor = outlineColor;
        this.fillColor = fillColor;
    }

    public GeometryStyle() {
    }

    public static GeometryStyle getDefaults(GeometryType type) {
        switch (type) {
            case WAY_POINT:
            case SEARCHER_POSITION:
                return new GeometryStyle(true, Color.black);
            case POSSIBLE_TREASURE:
                return new GeometryStyle(true, Color.gray, Color.lightGray);
        }
        return new GeometryStyle(true, Color.black);
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isFilled() {
        return this.filled;
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    public Color getOutlineColor() {
        return this.outlineColor;
    }

    public void setOutlineColor(Color outlineColor) {
        this.outlineColor = outlineColor;
    }

    public Color getFillColor() {
        return this.fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GeometryStyle)) {
            return false;
        }
        final GeometryStyle other = (GeometryStyle) o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.isVisible() != other.isVisible()) {
            return false;
        }
        if (this.isFilled() != other.isFilled()) {
            return false;
        }
        final Object this$outlineColor = this.getOutlineColor();
        final Object other$outlineColor = other.getOutlineColor();
        if (this$outlineColor == null ? other$outlineColor != null : !this$outlineColor.equals(other$outlineColor)) {
            return false;
        }
        final Object this$fillColor = this.getFillColor();
        final Object other$fillColor = other.getFillColor();
        return this$fillColor == null ? other$fillColor == null : this$fillColor.equals(other$fillColor);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof GeometryStyle;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isVisible() ? 79 : 97);
        result = result * PRIME + (this.isFilled() ? 79 : 97);
        final Object $outlineColor = this.getOutlineColor();
        result = result * PRIME + ($outlineColor == null ? 43 : $outlineColor.hashCode());
        final Object $fillColor = this.getFillColor();
        result = result * PRIME + ($fillColor == null ? 43 : $fillColor.hashCode());
        return result;
    }

    public String toString() {
        return "GeometryStyle(visible=" + this.isVisible() + ", filled=" + this.isFilled() + ", outlineColor=" + this.getOutlineColor() + ", fillColor=" + this.getFillColor() + ")";
    }
}
