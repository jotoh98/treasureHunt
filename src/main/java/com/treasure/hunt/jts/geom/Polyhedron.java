package com.treasure.hunt.jts.geom;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.jts.awt.CanvasBoundary;
import com.treasure.hunt.utils.JTSUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Polyhedron defined by a list of {@link HalfPlane}s.
 */
@AllArgsConstructor
@NoArgsConstructor
public class Polyhedron implements Shapeable {

    /**
     * List of halfPlanes defining the polyhedron.
     */
    private List<HalfPlane> halfPlanes = new ArrayList<>();

    /**
     * List of polygon coordinates.
     */
    @Getter
    private List<Coordinate> polygon = new ArrayList<>();

    /**
     * Polyhedron constructor from {@link HalfPlane} list.
     *
     * @param halfPlanes half planes to construct the polyhedron
     */
    public Polyhedron(List<HalfPlane> halfPlanes) {
        halfPlanes.forEach(halfPlane -> halfPlane.setStrict(false));
        this.halfPlanes = halfPlanes;
        resolve();
    }

    /**
     * Polyhedron constructor from multiple {@link HalfPlane}s.
     *
     * @param halfPlanes half planes to construct the polyhedron
     */
    public Polyhedron(HalfPlane... halfPlanes) {
        this(Arrays.asList(halfPlanes));
    }

    /**
     * Add new {@link HalfPlane} to the polyhedron.
     * Resolves the resulting polygon.
     *
     * @param halfPlane half plane to be added
     */
    public void addHalfPlane(HalfPlane halfPlane) {
        if (inside(halfPlane)) {
            halfPlane.setStrict(false);
            halfPlanes.add(halfPlane);
            resolve();
        }
    }

    /**
     * Tests, if a {@link Coordinate} lays inside of the polyhedron.
     *
     * @param c tested coordinate
     * @return whether the tested coordinate lays inside of the polyhedron
     */
    public boolean inside(Coordinate c) {
        if (c == null) {
            return false;
        }
        return halfPlanes.stream().allMatch(halfPlane -> halfPlane.inside(c));
    }

    /**
     * Tests, if a {@link Ray} lays inside of the polyhedron.
     *
     * @param ray tested ray
     * @return whether the tested ray lays inside of the polyhedron
     */
    public boolean inside(Ray ray) {
        return halfPlanes.stream().anyMatch(halfPlane -> inside(halfPlane.intersection(ray)));
    }

    /**
     * Retrives whether the polyhedron is bound.
     * That means, that the polyhedron is closed in all
     * directions, which is equal to the fact, that there
     * are as much intersections as necessary halfplanes.
     *
     * @return whether the polyhedron is bound
     */
    public boolean isBound() {
        return halfPlanes.size() == polygon.size();
    }

    /**
     * Retrives whether the polyhedron is unbound.
     * The direct opposite of being bound.
     *
     * @return whether the polyhedron is unbound
     */
    public boolean isUnbound() {
        return !isBound();
    }

    /**
     * <p>Resolve the polyhedron's vertices destructively.</p>
     * <p>First, we sort the half planes by their angle relative to the x-axis. With this we can assert that
     *     <ol type="a">
     *         <li>every neighbour between two intersecting {@link HalfPlane}s can safely be deleted without
     *         damaging the {@link Polyhedron} and</li>
     *         <li>that there is no need to search further than the nearest intersecting {@link HalfPlane}.</li>
     *     </ol>
     * </p>
     * <p>Then, we iterate over all the successor {@link HalfPlane}s logging, if the half plane is necessary for the
     * polyhedron, until
     *     <ol type="a">
     *         <li>we find an checked intersection laying in the polyhedron (checked via Ax&ge;b or Ax>b), or</li>
     *         <li>every other half plane was checked for an intersection unsuccessfully.</li>
     *     </ol>
     *     First, we check if the angle between the current and the next {@link HalfPlane} is negative. If so, we
     *     found the unbound section, both {@link HalfPlane}s are necessary. We "jump" across the gap and continue
     *     with the 2nd of the two unbound {@link HalfPlane}s.
     * </p>
     * <p>
     *     If there is no gap, we intersect the neighbours and check the intersection:
     *     <ul>
     *         <li>If the intersection lays not int the convex {@link Polyhedron}, the <code>other</code>
     *         {@link HalfPlane} is unnecessary, we continue with the next neighbour.</li>
     *         <li>If it lays in the {@link Polyhedron}, both {@link HalfPlane}s are marked necessary and everything
     *         between the two intersecting {@link HalfPlane}s stays marked unnecessary. We continue to search from this
     *         neighbour on.</li>
     *     </ul>
     * </p>
     * <p>At the end, we check the premise of the polyhedron to be bound; thus if there are as many half planes as
     * intersections. If that's not the case, the last half plane could'nt find an intersection rendering it unnecessary.
     * But we</p>
     */
    public void resolve() {

        List<Coordinate> coordinates = new ArrayList<>();

        final List<HalfPlane> sorted = sortByAngle(halfPlanes);

        final int halfPlaneAmount = sorted.size();

        final List<Boolean> necessities = new ArrayList<>(Collections.nCopies(halfPlaneAmount, false));

        for (int currentIndex = 0; currentIndex < halfPlaneAmount; ) {
            final HalfPlane current = sorted.get(currentIndex);
            boolean intersected = false;
            for (int offset = 1; offset < halfPlaneAmount; offset++) {
                final int otherIndex = (currentIndex + offset) % halfPlaneAmount;
                final HalfPlane other = sorted.get(otherIndex);

                if (offset == 1 && formingUnbound(current, other)) {
                    necessities.set(currentIndex, true);
                    necessities.set(otherIndex, true);
                    break;
                }

                final Coordinate intersection = current.intersection(other);

                if (intersection != null && inside(intersection)) {
                    coordinates.add(intersection);
                    necessities.set(currentIndex, true);
                    necessities.set(otherIndex, true);
                    currentIndex = otherIndex;
                    intersected = true;
                    break;
                }
            }
            if (!intersected) {
                currentIndex++;
            } else if (currentIndex == 0) {
                break;
            }
        }

        filterNaive(sorted, necessities);

        polygon = coordinates;
    }

    public void resolveNaive() {
        final Stream<HalfPlane> halfPlaneStream = this.halfPlanes.stream();

        Coordinate[] coordinates = halfPlaneStream
                .flatMap(firstHalfPlane -> halfPlaneStream
                        .filter(otherHalfPlane -> !otherHalfPlane.equals(firstHalfPlane))
                        .map(two -> two.intersection(firstHalfPlane))
                )
                .distinct()
                .filter(this::inside)
                .toArray(Coordinate[]::new);

        ConvexHull convexHull = new ConvexHull(coordinates, JTSUtils.GEOMETRY_FACTORY);
        this.polygon = Arrays.asList(convexHull.getConvexHull().getCoordinates());
    }


    /**
     * Retrieve if two {@link HalfPlane}s are forming an unbound section.
     * Unbound section means that the directional vector of the right and the directional vector of the left
     * {@link HalfPlane} rotated by 180 degrees diverge forming an unbound infinite section.
     *
     * @param right right half plane compared
     * @param left  left half plane compared
     * @return if right and left half planes form an unbound section
     */
    private boolean formingUnbound(HalfPlane right, HalfPlane left) {
        return right.getDirection().angle() <= left.getDirection().rotateByQuarterCircle(2).angle();
    }

    /**
     * Filter the {@link HalfPlane}s on whether or not they are necessary for the polyhedron.
     *
     * @param halfPlanes the list of half planes
     * @param necessary  the list of necessities
     */
    private void filterNaive(List<HalfPlane> halfPlanes, List<Boolean> necessary) {
        if (halfPlanes.size() != necessary.size()) {
            return;
        }
        this.halfPlanes = IntStream.range(0, necessary.size())
                .filter(necessary::get)
                .mapToObj(halfPlanes::get)
                .collect(Collectors.toList());
    }

    private void filterNaive(List<HalfPlane> halfPlanes) {
        this.halfPlanes = halfPlanes.stream()
                .filter(halfPlane -> polygon.stream().anyMatch(halfPlane::inside))
                .collect(Collectors.toList());
    }

    private List<HalfPlane> getUnbound() {
        return halfPlanes.stream()
                .filter(halfPlane -> polygon.stream().filter(halfPlane::inside).count() == 1)
                .collect(Collectors.toList());
    }

    /**
     * Sort the list of {@link HalfPlane}s by the angle relative to the x-axis.
     * The angle is represented as the (2 &sdot; &pi;)-normalized angle.
     *
     * @param halfPlanes list of half planes to sort
     * @return sorted half planes by angle
     */
    private List<HalfPlane> sortByAngle(List<HalfPlane> halfPlanes) {
        return halfPlanes.stream()
                .sorted(Comparator.comparingDouble(halfPlane -> twoPiAngle(halfPlane.getDirection())))
                .collect(Collectors.toList());
    }

    /**
     * Get the (2 &sdot; &pi;)-normalized angle for a direction vector.
     *
     * @param direction direction to retrieve the angle from
     * @return the (2 &sdot; &pi;)-normalized angle relative to the x-axis
     */
    private double twoPiAngle(Vector2D direction) {
        double angle1 = direction.angle();
        if (angle1 < 0) {
            angle1 += 2 * Math.PI;
        }
        return angle1;
    }

    public Geometry getGeometry(boolean ignoreUnbound) {
        final ConvexHull convexHull = JTSUtils.getConvexHull(polygon);
        final Coordinate[] coordinates = convexHull.getConvexHull().norm().getCoordinates();
        final GeometryFactory factory = JTSUtils.GEOMETRY_FACTORY;

        if (coordinates.length == 1) {
            return factory.createPoint(coordinates[0]);
        }

        if ((ignoreUnbound || isBound()) && coordinates.length > 2) {
            return factory.createPolygon(coordinates).norm();
        }

        return factory.createLineString(coordinates).norm();
    }

    /**
     * Get rays of {@link HalfPlane}s in unbound section.
     *
     * @return list of half plane rays
     */
    public List<Ray> getUnboundRays() {
        final List<HalfPlane> sorted = sortByAngle(halfPlanes);
        for (int index = 0; index < sorted.size() - 1; index++) {
            final HalfPlane current = sorted.get(index);
            final HalfPlane next = sorted.get(index + 1);
            if (formingUnbound(current, next)) {
                return Arrays.asList(new Ray(current.p0, current.p1), new Ray(next.p1, next.p0));
            }
        }
        return Collections.emptyList();
    }

    /**
     * Get plain polygon from resolved list of {@link Coordinate}s.
     *
     * @return polygon from resolved {@link Coordinate}s
     */
    public Polygon toPolygon() {
        return JTSUtils.createPolygon(polygon);
    }

    /**
     * Get bound and unbound polygon.
     *
     * @param boundary canvas boundary to extract line segments and corners
     * @return bound and unbound polygon
     */
    private Polygon toPolygon(CanvasBoundary boundary) {
        final List<Ray> rays = getUnboundRays();

        List<Coordinate> coordinates = new ArrayList<>();
        if (isUnbound()) {
            coordinates = boundary
                    .getLineSegments()
                    .stream()
                    .flatMap(lineSegment -> rays
                            .stream()
                            .map(ray -> ray.intersection(lineSegment))
                    )
                    .collect(Collectors.toList());
            coordinates.addAll(boundary
                    .getCoordinates()
                    .stream()
                    .filter(this::inside)
                    .collect(Collectors.toList()));
        }
        coordinates.addAll(polygon);

        final Geometry geometry = JTSUtils.createPolygon(coordinates).norm();
        return geometry instanceof Polygon ? (Polygon) geometry : null;
    }

    /**
     * Get shape for polyhedron.
     *
     * @param advancedShapeWriter shape writer holding the {@link CanvasBoundary}
     * @return polyhedron shape (bound & unbound)
     */
    @Override
    public Shape toShape(AdvancedShapeWriter advancedShapeWriter) {
        final Polygon polygon = toPolygon(advancedShapeWriter.getBoundary());
        if (polygon == null) {
            return null;
        }
        return advancedShapeWriter.toShape(polygon);
    }
}
