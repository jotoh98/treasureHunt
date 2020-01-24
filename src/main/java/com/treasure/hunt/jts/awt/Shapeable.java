package com.treasure.hunt.jts.awt;

import java.awt.*;

/**
 * @author jotoh
 */
public interface Shapeable {
    Shape toShape(AdvancedShapeWriter advancedShapeWriter);
}
