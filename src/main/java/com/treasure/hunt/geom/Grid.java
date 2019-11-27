package com.treasure.hunt.geom;

import com.treasure.hunt.jts.PointTransformation;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.awt.*;

@AllArgsConstructor
@NoArgsConstructor
public class Grid implements Shapeable {
    boolean xDirection = true;
    boolean yDirection = true;

    @Override
    public Shape toShape(PointTransformation pointTransformation) {

        return null;
    }
}
