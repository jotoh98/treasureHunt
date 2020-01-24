package com.treasure.hunt.jts.awt;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;

/**
 * @author jotoh
 */
@AllArgsConstructor
@NoArgsConstructor
public class Grid implements Shapeable {
    boolean xDirection = true;
    boolean yDirection = true;

    @Override
    public Shape toShape(AdvancedShapeWriter shapeWriter) {
        getGridLines(shapeWriter.getPointTransformation());
        return null;
    }

    public ArrayList<Line2D> getGridLines(PointTransformation transformation) {
        double scale = transformation.getScaleProperty().get();

        ArrayList<Line2D> lineArray = new ArrayList<>();

        return null;
    }

    public int getSpacing(double scale) {
        return 0;
    }
}
