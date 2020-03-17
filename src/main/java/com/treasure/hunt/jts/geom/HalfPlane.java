package com.treasure.hunt.jts.geom;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.jts.awt.CanvasBoundary;
import com.treasure.hunt.utils.JTSUtils;
import com.treasure.hunt.utils.ListUtils;
import org.decimal4j.util.DoubleRounder;
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

/**
 * <p>A half plane divides the 2d-space in two subspaces being divided at a certain infinite line.
 * This implementation holds a coordinate representation of this line. It evaluates coordinates of being inside by
 * computing a normal vector pointing out of the inner half space and a corresponding double scalar to scale the half
 * plane away from the origin.</p>
 * <hr/>
 * <p>Now we want to explore, how we compute a vector laying inside the inner half space in {@link #inside(Vector2D)}:</p>
 * <p>So we state that <i>a</i> is the normal vector pointing out of the inner half space, <i>b</i> is the scalar and
 * <i>x</i> is the vector to test.</p>
 * <p>If evaluation is strict, the test evaluates true, if</p>
 * <center>&lang; a, x &rang; > b.</center>
 * <p>Else, it already evaluates true, if the following statement is true:</p>
 * <center>&lang; a, x &rang; &ge; b.</center>
 * <hr/>
 * <p>The half plane is represented as an infinite line in coordinate form. To perform {@link #inside(Vector2D)}, we
 * compute the corresponding normal vector and scalar. For two coordinates <i>c1</i> and <i>c2</i>, the inner half space
 * lies on the <u>right-hand-side</u> of the vector from <i>c1</i> to <i>c2</i>.</p>
 *
 * @see #inside(Vector2D)
 */
public class HalfPlane extends Line {

    /**
     * Strict decides whether or not to include points on the threshold line.
     * If strict is true, points on the line (&lang; a, x &rang; = b) will be excluded (&lang; a, x &rang; > b), otherwise,
     * these will be included (&lang; a, x &rang; &ge; b) by the {@link HalfPlane#inside(Coordinate)} method.
     *
     * @see HalfPlane#inside(Coordinate)
     */
    boolean strict;

    /**
     * Default constructor.
     * The half plane divides the space in two sections. The side, that the method {@link #inside(Vector2D)} evaluates true on
     * is on the right side of the vector from c1 to c2.
     *
     * @param c1     first coordinate the half planes line is going through
     * @param c2     second coordinate the half planes line is going through
     * @param strict whether points on the line are not inside the half plane
     */
    public HalfPlane(Coordinate c1, Coordinate c2, boolean strict) {
        super(c1, c2);
        this.p1 = c2;
        this.strict = strict;
    }

    /**
     * Convenience constructor for non-strict half planes.
     *
     * @param c1 first coordinate the half planes line is going through
     * @param c2 second coordinate the half planes line is going through
     */
    public HalfPlane(Coordinate c1, Coordinate c2) {
        this(c1, c2, false);
    }


    /**
     * Construct a HalfPlane with its normal vector and its scalar.
     * The vector and scalar are the matrix representation components of the half plane.
     * These components are directly involved in determining, if a coordinate lies inside the half lane.
     *
     * @param normal normal vector of the half plane pointing outwards
     * @param scalar scalar for translating the half plane away from the origin point
     * @return a half plane in coordinate representation
     * @see #inside(Vector2D)
     */
    public static HalfPlane from(Vector2D normal, double scalar) {
        if (normal.getX() == 0 && normal.getY() == 0) {
            throw new IllegalArgumentException("A half plane needs a non trivial direction vector");
        }

        final Vector2D direction = normal.rotateByQuarterCircle(-1);

        final Coordinate basis = normal.getY() == 0
                ? new Coordinate(scalar / normal.getX(), 0)
                : new Coordinate(0, scalar / normal.getY());

        return new HalfPlane(basis, direction.translate(basis));
    }

    /**
     * Directional vector of the divider line.
     *
     * @return divider direction vector
     */
    public Vector2D getDirection() {
        return Vector2D.create(p0, p1);
    }

    /**
     * Compute the normal vector for evaluating {@link #inside(Vector2D)}.
     * The normal vector is the directional vector rotated clockwise by 90&deg;.
     *
     * @return half plane normal vector
     */
    public Vector2D getNormalVector() {
        return getDirection().rotateByQuarterCircle(-1);
    }

    /**
     * Compute the scalar for evaluating {@link #inside(Vector2D)}.
     *
     * @return evaluation scalar
     */
    public double getScalar() {
        final Vector2D vector = getNormalVector();
        return vector.getX() * p0.x + vector.getY() * p0.y;

    }

    /**
     * Evaluates, if a location vector of a point lies in the inner half space.<br/>
     * For strict half planes, it must satisfy the following condition:
     * <center>&lang; a, x &rang; > b,</center>
     * otherwise, it tests the condition:
     * <center>&lang; a, x &rang; &ge; b.</center>
     *
     * @param x location vector to test
     * @return whether the point lays in the inner half space
     */
    public boolean inside(Vector2D x) {
        final double dotProduct = DoubleRounder.round(x.dot(getNormalVector()), 10);
        final double scalar = DoubleRounder.round(getScalar(), 10);
        return strict ? dotProduct > scalar : dotProduct >= scalar;
    }

    /**
     * Convenience method for {@link #inside(Vector2D)}.
     *
     * @param c coordinate to test
     * @return whether the coordinate lays in the inner half space
     */
    public boolean inside(Coordinate c) {
        return inside(Vector2D.create(c));
    }

    public boolean inside(LineSegment lineSegment) {
        return inside(lineSegment.p0) || inside(lineSegment.p1);
    }

    public boolean covers(Geometry g) {
        return ListUtils.allMatch(g.getCoordinates(), this::inside);
    }

    public boolean covers(LineSegment lineSegment) {
        return inside(lineSegment.p0) && inside(lineSegment.p1);
    }

    @Override
    public Shape toShape(AdvancedShapeWriter shapeWriter) {
        final Polygon polygon = toPolygon(shapeWriter.getBoundary());
        return polygon == null ? null : shapeWriter.toShape(polygon);
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

    public boolean equals(HalfPlane other) {
        return p0.equals2D(other.p0) && p1.equals2D(other.p1);
    }
}
