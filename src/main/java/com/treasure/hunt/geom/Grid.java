package com.treasure.hunt.geom;

import com.treasure.hunt.jts.PointTransformation;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
public class Grid implements Shapeable {
    boolean xDirection = true;
    boolean yDirection = true;

    @Override
    public Shape toShape(PointTransformation pointTransformation) {
        //getGridLines(pointTransformation.getRightLowerBoundary(),pointTransformation.getLeftUpperBoundary());
        return null;
    }

    public ArrayList<Line2D> getGridLines(Vector2D rightlower, Vector2D leftupper) {
        double viewHeight = rightlower.getY() - leftupper.getY();
        double viewLength = rightlower.getX() - leftupper.getX();

        return null;
    }
}
