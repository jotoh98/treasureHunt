package com.treasure.hunt.strategy.geom;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.jfree.fx.FXGraphics2D;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Classifies a jts geometry item with parameters to distinguish between items for visualization/algorithm usages.
 *
 * @author jotoh, dorianreineccius
 * @see GeometryType for further information about how to classifiy a geometry item.
 */
@Getter
@EqualsAndHashCode(of = {"object", "geometryType"})
public class GeometryItem<T> {
    @NonNull
    @Getter
    private T object;
    @NonNull
    private GeometryType geometryType;
    @Getter
    private List<GeometryStyle> geometryStyles = new ArrayList<>();
    private int preferredStyle = 0;

    /**
     * The constructor.
     *
     * @param object        the {@link org.locationtech.jts.geom.Geometry} or {@link com.treasure.hunt.jts.geom.Shapeable}.
     * @param geometryType  the {@link GeometryType}, defining its role.
     * @param geometryStyle the {@link GeometryStyle}, defining its looking.
     */
    public GeometryItem(T object, GeometryType geometryType, GeometryStyle geometryStyle) {
        assert (object != null);
        this.object = object;
        this.geometryType = geometryType;
        this.geometryStyles.add(geometryStyle);
    }

    /**
     * The constructor, using default {@link GeometryStyle}.
     *
     * @param object       the {@link org.locationtech.jts.geom.Geometry} or {@link com.treasure.hunt.jts.geom.Shapeable}.
     * @param geometryType the {@link GeometryType}, defining its role.
     */
    public GeometryItem(T object, GeometryType geometryType) {
        this(object, geometryType, GeometryStyle.getDefaults(geometryType));
    }


    public GeometryStyle getGeometryStyle() {
        return geometryStyles.get(preferredStyle);
    }

    public void setPreferredStyle(int preferredStyle) {
        this.preferredStyle = Math.max(0, Math.min(geometryStyles.size() - 1, preferredStyle));
    }

    public void addGeometryStyle(GeometryStyle style) {
        geometryStyles.add(style);
    }
}