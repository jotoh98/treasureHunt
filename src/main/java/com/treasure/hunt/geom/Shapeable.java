package com.treasure.hunt.geom;

import com.treasure.hunt.jts.PointTransformation;

import java.awt.*;

public interface Shapeable {
    Shape toShape(PointTransformation pointTransformation);
}
