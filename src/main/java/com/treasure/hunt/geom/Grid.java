package com.treasure.hunt.geom;

import com.treasure.hunt.jts.AdvancedShapeWriter;
import com.treasure.hunt.jts.CanvasBoundary;
import com.treasure.hunt.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;

@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Grid implements Shapeable {
    @Getter
    @Setter
    private boolean visibleHorizontal = true;

    @Getter
    @Setter
    private boolean visibleVertical = true;


    private double minDistance = 40d;

    private double maxDistance = 80d;

    private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

    private DecimalFormat df = new DecimalFormat("###.###");

    @Override
    public Shape toShape(AdvancedShapeWriter shapeWriter) {
        GeneralPath generalPath = new GeneralPath();

        final double distance = getDistance(shapeWriter);

        verticalLines(shapeWriter, generalPath, distance);

        horizontalLines(shapeWriter, generalPath, distance);

        return generalPath;
    }

    private double getDistance(AdvancedShapeWriter shapeWriter) {

        final double scale = shapeWriter.getPointTransformation().getScale();

        double ratio = maxDistance / minDistance;
        int log2 = (int) (Math.log(scale) / Math.log(ratio));

        if (scale < 1) {
            log2 -= 1;
        }

        return Math.pow(ratio, -log2) * minDistance;
    }

    private void verticalLines(AdvancedShapeWriter shapeWriter, GeneralPath generalPath, double distance) {
        final CanvasBoundary boundary = shapeWriter.getBoundary();
        final Vector2D dimensions = boundary.getCanvasDimensions();

        final double lowerX = Math.floor(boundary.getMinX() / distance) * distance;
        final double upperX = Math.ceil(boundary.getMaxX() / distance) * distance;
        for (double display = lowerX; display < upperX; display += distance) {

            //TODO: fix early clipping from right side
            final double x = shapeWriter.getPointTransformation().transformX(display);

            generalPath.append(new Line2D.Double(x, 0, x, dimensions.getY()), false);

            final Shape text = RenderUtils.shapeFromText(font, df.format(display), x, dimensions.getY() - 8);
            generalPath.append(text, false);
        }
    }

    private void horizontalLines(AdvancedShapeWriter shapeWriter, GeneralPath generalPath, double distance) {
        final CanvasBoundary boundary = shapeWriter.getBoundary();
        final Vector2D dimensions = boundary.getCanvasDimensions();

        final double lowerY = Math.floor(boundary.getMinY() / distance) * distance;
        final double upperY = Math.ceil(boundary.getMaxY() / distance) * distance;
        for (double display = lowerY; display < upperY; display += distance) {

            //TODO: doesn't display
            final double y = shapeWriter.getPointTransformation().transformX(display);

            generalPath.append(new Line2D.Double(0, y, dimensions.getX(), y), false);

            final Shape text = RenderUtils.shapeFromText(font, df.format(display), 2, y + 4);
            generalPath.append(text, false);
        }
    }
}
