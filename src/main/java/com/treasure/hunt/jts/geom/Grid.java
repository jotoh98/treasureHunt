package com.treasure.hunt.jts.geom;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.jts.awt.CanvasBoundary;
import com.treasure.hunt.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;

/**
 * A visual grid to set the scaling in perspective.
 * Jumps to scale between a min and max rendering distance.
 *
 * @author jotoh
 */
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Grid implements Shapeable {
    /**
     * Whether or not horizontal grid lines are rendered.
     */
    @Getter
    @Setter
    private boolean horizontalVisible = true;

    /**
     * Whether or not vertical grid lines are rendered.
     */
    @Getter
    @Setter
    private boolean verticalVisible = true;

    /**
     * Minimal rendering distance between two grid lines.
     */
    private static final double MIN_DISTANCE = 40d;

    /**
     * Maximal rendering distance between two grid lines.
     */
    private static final double MAX_DISTANCE = 80d;

    /**
     * Font for grid line identification number.
     */
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

    /**
     * Formatter for grid line identification numbers.
     */
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###.###");

    /**
     * Get a shape of the grid.
     *
     * @param shapeWriter shape writer holding information for boundary and scaling
     * @return shape from grid
     */
    @Override
    public Shape toShape(AdvancedShapeWriter shapeWriter) {
        GeneralPath generalPath = new GeneralPath();

        final double distance = getDistance(shapeWriter.getPointTransformation().getScale());

        if (verticalVisible) {
            verticalLines(shapeWriter, generalPath, distance);
        }
        if (horizontalVisible) {
            horizontalLines(shapeWriter, generalPath, distance);
        }

        return generalPath;
    }

    /**
     * Get jumping distance between two grid lines.
     *
     * @param scale uncorrected scale of the point transformation
     * @return corrected (jumping) scale for spacing between grid lines
     */
    private double getDistance(double scale) {
        double ratio = MAX_DISTANCE / MIN_DISTANCE;
        int log2 = (int) (Math.log(scale) / Math.log(ratio));

        if (scale < 1) {
            log2 -= 1;
        }

        return Math.pow(ratio, -log2) * MIN_DISTANCE;
    }

    /**
     * Add the vertical lines to the {@link GeneralPath}.
     *
     * @param shapeWriter shape writer transforming the grid lines
     * @param generalPath path the grid lines should be appended to
     * @param distance    distance between two grid lines
     */
    private void verticalLines(AdvancedShapeWriter shapeWriter, GeneralPath generalPath, double distance) {
        final CanvasBoundary boundary = shapeWriter.getBoundary();
        final double canvasHeight = boundary.getCanvasHeight().get();

        final double lowerX = Math.floor(boundary.getMinX() / distance) * distance;
        final double upperX = Math.ceil(boundary.getMaxX() / distance) * distance;
        for (double lineNumber = lowerX; lineNumber < upperX; lineNumber += distance) {
            final double x = shapeWriter.getPointTransformation().transformX(lineNumber);
            generalPath.append(vertical(x, canvasHeight), false);
            generalPath.append(renderNumber(lineNumber, x, canvasHeight - 8), false);
        }
    }

    /**
     * Add the horizontal lines to the {@link GeneralPath}.
     *
     * @param shapeWriter shape writer transforming the grid lines
     * @param generalPath path the grid lines should be appended to
     * @param distance    distance between two grid lines
     */
    private void horizontalLines(AdvancedShapeWriter shapeWriter, GeneralPath generalPath, double distance) {
        final CanvasBoundary boundary = shapeWriter.getBoundary();
        final double canvasWidth = boundary.getCanvasWidth().get();

        final double lowerY = Math.floor(boundary.getMinY() / distance) * distance;
        final double upperY = Math.ceil(boundary.getMaxY() / distance) * distance;
        for (double lineNumber = lowerY; lineNumber < upperY; lineNumber += distance) {
            final double y = shapeWriter.getPointTransformation().transformY(lineNumber);
            generalPath.append(horizontal(y, canvasWidth), false);
            generalPath.append(renderNumber(lineNumber, 2, y + 4), false);
        }
    }

    private Line2D.Double vertical(double x, double height) {
        return new Line2D.Double(x, 0, x, height);
    }

    private Line2D.Double horizontal(double y, double width) {
        return new Line2D.Double(0, y, width, y);
    }

    private Shape renderNumber(double number, double x, double y) {
        return RenderUtils.shapeFromText(FONT, DECIMAL_FORMAT.format(number), x, y);
    }
}
