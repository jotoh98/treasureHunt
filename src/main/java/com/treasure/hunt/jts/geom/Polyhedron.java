package com.treasure.hunt.jts.geom;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.utils.JTSUtils;
import com.treasure.hunt.utils.ListUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Polyhedron defined by a list of {@link HalfPlane}s.
 */
@AllArgsConstructor
@NoArgsConstructor
public class Polyhedron implements Shapeable {

    /**
     * List of halfPlanes defining the polyhedron.
     */
    @Getter
    private List<HalfPlane> halfPlanes = new ArrayList<>();

    @Getter
    private List<Coordinate> resolved = new ArrayList<>();

    private List<Ray> unbound = new ArrayList<>();

    private List<LineSegment> resolvedList = new ArrayList<>();

    public Polyhedron(List<HalfPlane> halfPlanes) {
        this.halfPlanes = halfPlanes;
        resolve();
    }

    public void addHalfPlane(HalfPlane halfPlane) {
        if (inside(halfPlane)) {
            halfPlanes.add(halfPlane);
            resolve();
        }
    }

    public void addAngle(GeometryAngle angle) {
        addHalfPlane(new HalfPlane(angle.getCenter(), angle.getRight()));
        addHalfPlane(new HalfPlane(angle.getLeft(), angle.getCenter()));
    }

    /**
     * Tests, if a {@link Ray} lays inside of the polyhedron.
     *
     * @param ray tested ray
     * @return whether the tested ray lays inside of the polyhedron
     */
    public boolean inside(Ray ray) {
        if (halfPlanes.size() == 0) {
            return true;
        }
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
        return halfPlanes.size() == resolved.size();
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
     * Tests, if a {@link Coordinate} lays inside of the polyhedron.
     *
     * @param c tested coordinate
     * @return whether the tested coordinate lays inside of the polyhedron
     */
    public boolean inside(Coordinate c) {
        if (c == null) {
            return false;
        }
        return halfPlanes.stream().anyMatch(halfPlane -> halfPlane.inside(c));
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
     *     found the unbounded section, both {@link HalfPlane}s are necessary. We "jump" across the gap and continue
     *     with the 2nd of the two unbounded {@link HalfPlane}s.
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

        resolved = new ArrayList<>();

        sortByAngle();

        final int size = halfPlanes.size();

        for (int outerIndex = 0; outerIndex < size; outerIndex++) {
            for (int innerIndex = 0; innerIndex < size; innerIndex++) {
                if (innerIndex == outerIndex) {
                    continue;
                }

                int nextIndex = (outerIndex + 1) % size;

                final HalfPlane current = halfPlanes.get(outerIndex);
                final HalfPlane other = halfPlanes.get(innerIndex);

                Coordinate intersection = current.intersection(other);

                if (inside(intersection)) {
                    resolved.add(intersection);
                }
            }
        }

        filterHalfPlanes();

        if (isUnbound()) {
        }
    }

    private boolean formsUnbound(HalfPlane h1, HalfPlane h2) {
        return h1.getDirection().angle() <= h2.getDirection().rotateByQuarterCircle(2).angle();
    }

    /**
     * Filter the {@link HalfPlane}s on whether or not they are necessary for the polyhedron.
     */
    private void filterHalfPlanes() {

        Coordinate[] coordinates = getConvexHull().getCoordinates();
        List<LineSegment> lineSegments = ListUtils
                .consecutive(coordinates, LineSegment::new)
                .collect(Collectors.toList());

        lineSegments.add(new LineSegment(coordinates[0], coordinates[coordinates.length - 1]));

        halfPlanes = halfPlanes.stream()
                .filter(halfPlane ->
                        lineSegments.stream().anyMatch(lineSegment ->
                                halfPlane.intersection(lineSegment) != null
                        )
                )
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

    private void sortByAngle() {
        halfPlanes = sortByAngle(halfPlanes);
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

    public Geometry getGeometry() {
        ConvexHull convexHull = new ConvexHull(resolved.toArray(Coordinate[]::new), JTSUtils.GEOMETRY_FACTORY);

        Geometry hullGeometry = convexHull.getConvexHull();

        if (isBound()) {
            return hullGeometry;
        }

        List<Coordinate> coordinates = Arrays.asList(hullGeometry.norm().getCoordinates());

        Coordinate firstUnbound = resolvedList.stream()
                .filter(lineSegment -> lineSegment instanceof Ray)
                .findFirst()
                .map(ray -> ray.p0)
                .orElse(null);

        int startIndex = coordinates.indexOf(firstUnbound);

        if (startIndex >= 0 && coordinates.size() > 1) {
            Collections.rotate(coordinates, startIndex);

            return JTSUtils
                    .GEOMETRY_FACTORY
                    .createLineString(coordinates.toArray(Coordinate[]::new))
                    .norm();
        }

        return hullGeometry;

    }


    public List<Ray> getRays() {
        if (isBound()) {
            return new ArrayList<>();
        }

        return resolvedList.stream()
                .filter(lineSegment -> lineSegment instanceof Ray)
                .map(lineSegment -> (Ray) lineSegment)
                .collect(Collectors.toList());
    }

    public Geometry getConvexHull() {
        return JTSUtils.createConvexHull(resolved).getConvexHull();
    }

    public void intersect(Ray ray) {

        List<Coordinate> inters = new ArrayList<>();
        for (HalfPlane plane : halfPlanes) {
            if (ray.intersection(plane) == null) {
                inters.add(ray.intersection(plane));
            }
        }
        inters.stream()
                .filter(this::inside)
                .collect(Collectors.toList());
    }

    @Override
    public Shape toShape(AdvancedShapeWriter advancedShapeWriter) {
        Geometry convexHull = JTSUtils.createConvexHull(getResolved()).getConvexHull();

        Shape hullShape = advancedShapeWriter.toShape(convexHull);

        if (isBound()) {
            return hullShape;
        }

        GeneralPath path = new GeneralPath();

        path.append(hullShape, false);
        getRays().forEach(ray -> path.append(advancedShapeWriter.toShape(ray), false));
        return path;
    }
}
