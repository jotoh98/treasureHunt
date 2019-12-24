package com.treasure.hunt.jts.awt;

import com.treasure.hunt.jts.geom.Shapeable;
import com.treasure.hunt.utils.JTSUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * Enhances the jts {@link ShapeWriter} with handling the render process for {@link Shapeable} instances.
 * In general, it shares a {@link PointTransformation} object to translate the {@link Shapeable}'s
 * Geometry into translated and scaled awt {@link Shape}'s.
 *
 * @author jotoh
 * @version 1.0
 */
@Slf4j
public class AdvancedShapeWriter extends ShapeWriter {

    /**
     * The {@link PointTransformation} transforming a source {@link Coordinate}.
     */
    @Getter
    private PointTransformation pointTransformation;

    /**
     * The boundary of the associated {@link javafx.scene.canvas.Canvas}.
     * Enables boundary specific shapes like endless lines, grids etc.
     */
    @Setter
    @Getter
    private CanvasBoundary boundary;

    /**
     * Constructor for AdvancedShapeWriter.
     *
     * @param pointTransformation {@link PointTransformation} used in rendering process.
     */
    public AdvancedShapeWriter(PointTransformation pointTransformation) {
        super(pointTransformation);
        this.pointTransformation = pointTransformation;
    }

    /**
     * Wrapper for the boundary's update method.
     */
    public void updateBoundary() {
        boundary.update();
    }

    /**
     * Wrapper function for {@link ShapeWriter#toShape(Geometry)} to extend functionality for {@link Shapeable} instances.
     *
     * @param object object to transfer to a shape
     * @return {@link Shape} representing the object
     */
    public Shape toShape(Object object) {
        if (object instanceof Shapeable) {
            return ((Shapeable) object).toShape(this);
        }
        try {
            return super.toShape((Geometry) object);
        } catch (IllegalArgumentException e) {
            log.debug("Could not render object to shape", e);
        }
        return null;
    }

    /**
     * Wrapper transform function for {@link AdvancedShapeWriter#transform(Coordinate, Point2D)}
     *
     * @param src  source Coordinate
     * @param dest point we want to transform
     */
    public void transform(Coordinate src, Point2D dest) {
        pointTransformation.transform(src, dest);
    }

    /**
     * Utility function to construct an already transformed {@link Line2D}.
     *
     * @param from start {@link Coordinate}
     * @param to   end {@link Coordinate}
     * @return {@link Line2D} {@link Shape} representing the line
     */
    public Line2D createLine(Coordinate from, Coordinate to) {
        Point2D.Double start = new Point2D.Double();
        Point2D.Double end = new Point2D.Double();
        pointTransformation.transform(from, start);
        pointTransformation.transform(to, end);

        return new Line2D.Double(start.x, start.y, end.x, end.y);
    }

    /**
     * Utility function to construct an already transformed {@link Line2D} with non-scalable length.
     *
     * @param fixed    start {@link Coordinate}
     * @param floating direction {@link Coordinate}
     * @param length   fixed length the line is rendered with
     * @return {@link Line2D} {@link Shape} representing the line
     */
    public Line2D createFixedLine(Coordinate fixed, Coordinate floating, double length) {
        Coordinate start = pointTransformation.transform(fixed);
        Vector2D vector2D = JTSUtils.normalizedVector(fixed, floating).multiply(length);
        Coordinate end = JTSUtils.negateY(vector2D).translate(start);
        return new Line2D.Double(start.x, start.y, end.x, end.y);
    }

    /**
     * Utility function to construct an already transformed {@link Arc2D} with non-scalable width and height.
     *
     * @param center    center {@link Coordinate} the arc is positioned around
     * @param dimension width and height of the arc
     * @param start     start angle relative to x-axis in radians
     * @param extend    extend angle (relative to start angle) in radians
     * @return transformed open {@link Arc2D}
     */
    public Arc2D createArc(Coordinate center, double dimension, double start, double extend) {
        Point2D point2D = new Point2D.Double();

        transform(center, point2D);
        return new Arc2D.Double(
                point2D.getX() - dimension / 2,
                point2D.getY() - dimension / 2,
                dimension,
                dimension,
                Math.toDegrees(start),
                Math.toDegrees(extend),
                Arc2D.OPEN
        );
    }
}
