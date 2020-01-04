package com.treasure.hunt.geom;

import com.treasure.hunt.jts.AdvancedShapeWriter;

import java.awt.*;

public interface Shapeable {
    Shape toShape(AdvancedShapeWriter advancedShapeWriter);
}
