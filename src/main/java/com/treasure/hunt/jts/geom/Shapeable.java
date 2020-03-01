package com.treasure.hunt.jts.geom;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;

import java.awt.*;

/**
 * @author jotoh
 */
public interface Shapeable {
    Shape toShape(AdvancedShapeWriter advancedShapeWriter);
}
