package com.treasure.hunt.jts.geom;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.jts.awt.CanvasBoundary;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HalfPlane extends Line {

    /**
     * Strict decides whether or not to include points on the threshold line.
     * If strict is true, points on the line (Ax=b) will be excluded (Ax>b), otherwise,
     * these will be included (Ax>=b) by the {@link HalfPlane#inside(Coordinate)} method.
     *
     * @see HalfPlane#inside(Coordinate)
     */
    boolean strict;

    public HalfPlane(Coordinate c1, Coordinate c2, boolean strict) {
        super(c1, c2);
        this.p1 = c2;
        this.strict = strict;
    }

    public HalfPlane(Coordinate c1, Coordinate c2) {
        this(c1, c2, false);
    }

    /**
     * Construct a HalfPlane
     *
     * @param normal
     * @param scalar
     * @return
     */
    public static HalfPlane from(Vector2D normal, double scalar) {
        if (JTSUtils.isNullVector(normal)) {
            throw new IllegalArgumentException("A half plane needs a non trivial direction vector");
        }

        final Vector2D direction = normal.rotateByQuarterCircle(-1);

        final Coordinate basis = normal.getY() == 0
                ? new Coordinate(scalar / normal.getX(), 0)
                : new Coordinate(0, scalar / normal.getY());

        return new HalfPlane(basis, direction.translate(basis));
    }

    public Vector2D getNormalVector() {
        return getDirection().rotateByQuarterCircle(1);
    }

    public double getScalar() {
        final Vector2D vector = getNormalVector();
        return -vector.getY() * p0.x + vector.getX() * p0.y;

    }

    public Vector2D getDirection() {
        return Vector2D.create(p0, p1);
    }

    public boolean covers(Geometry g) {
        for (Coordinate coordinate : g.getCoordinates()) {
            if (!inside(coordinate)) {
                return false;
            }
        }
        return true;
    }

    public boolean inside(Ray ray) {
        final Coordinate intersection = ray.intersection(toLine());
        return (intersection != null) || inside(ray.getCoordinate(0));
    }

    public boolean inside(Vector2D v) {
        final double dotProduct = v.dot(getNormalVector());
        final double scalar = getScalar();
        return strict ? dotProduct > scalar : dotProduct >= scalar;
    }

    public boolean inside(Coordinate c) {
        return inside(Vector2D.create(c));
    }

    public Line toLine() {
        return new Line(p0, p1);
    }

    public LineSegment toLineSegment() {
        return new LineSegment(p0, p1);
    }

    private Polygon toPolygon(CanvasBoundary boundary) {
        final List<Coordinate> intersections = JTSUtils.getBoundaryIntersections(boundary, this);

        final Stream<Coordinate> cornerStream = boundary.getCoordinates().stream()
                .filter(this::inside);

        final Coordinate[] coordinates = Stream
                .concat(intersections.stream(), cornerStream)
                .collect(Collectors.toList())
                .toArray(Coordinate[]::new);

        final ConvexHull convexHull = new ConvexHull(coordinates, JTSUtils.GEOMETRY_FACTORY);

        final Geometry geometry = convexHull.getConvexHull();

        if (geometry instanceof Polygon) {
            return (Polygon) geometry;
        }

        return null;
    }

    @Override
    public Shape toShape(AdvancedShapeWriter shapeWriter) {
        final Polygon polygon = toPolygon(shapeWriter.getBoundary());
        return polygon == null ? null : shapeWriter.toShape(polygon);
    }

    public boolean equals(HalfPlane other) {
        return p0.equals2D(other.p0) && p1.equals2D(other.p1);
    }
}
