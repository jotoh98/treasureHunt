package com.treasure.hunt.jts;

import com.treasure.hunt.geom.Shapeable;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Geometry;

import java.awt.*;

@Slf4j
public class AdvancedShapeWriter extends ShapeWriter {

    private PointTransformation pointTransformation;

    public AdvancedShapeWriter(PointTransformation pointTransformation) {
        super(pointTransformation);
        this.pointTransformation = pointTransformation;
    }

    public Shape toShape(Object object) {
        if (object instanceof Shapeable) {
            return ((Shapeable) object).toShape(pointTransformation);
        }
        try {
            return super.toShape((Geometry) object);
        } catch (IllegalArgumentException e) {
            log.debug("Could not render object to shape", e);
        }
        return null;
    }
}
