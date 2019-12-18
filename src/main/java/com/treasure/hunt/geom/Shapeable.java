package com.treasure.hunt.geom;

import com.treasure.hunt.jts.AdvancedShapeWriter;

import java.awt.*;

/**
 * @author jotoh
 */
public interface Shapeable {
    Shape toShape(AdvancedShapeWriter advancedShapeWriter);
}
