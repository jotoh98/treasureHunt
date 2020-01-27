package com.treasure.hunt.jts.geom;

import com.treasure.hunt.utils.JTSUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Polyhedron defined by a list of {@link HalfPlane}s.
 */
@AllArgsConstructor
@NoArgsConstructor
public class Polyhedron {

    /**
     * List of halfPlanes defining the polyhedron.
     */
    private List<HalfPlane> halfPlanes = new ArrayList<>();

    @Getter
    private List<Coordinate> resolved = new ArrayList<>();

    public Polyhedron(List<HalfPlane> halfPlanes) {
        this.halfPlanes = halfPlanes;
        resolve();
    }

    public void addHalfPlane(HalfPlane halfPlane) {
        if (inside(halfPlane)) {
            halfPlanes.add(halfPlane);
        }
    }

    /**
     * Tests, if a {@link Coordinate} lays inside of the polyhedron.
     *
     * @param c tested coordinate
     * @return whether the tested coordinate lays inside of the polyhedron
     */
    public boolean inside(Coordinate c) {
        return halfPlanes.stream().anyMatch(halfPlane -> halfPlane.inside(c));
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

        List<Coordinate> coordinates = new ArrayList<>();

        final List<HalfPlane> sorted = sortByAngle(halfPlanes);

        final int halfPlaneAmount = sorted.size();

        final List<Boolean> necessities = new ArrayList<>(Collections.nCopies(halfPlaneAmount, false));

        for (int currentIndex = 0; currentIndex < halfPlaneAmount; ) {
            final HalfPlane current = sorted.get(currentIndex);
            boolean intersected = false;
            for (int otherIndexOffset = 1; otherIndexOffset < halfPlaneAmount - 2; otherIndexOffset++) {
                final int otherIndex = (currentIndex + otherIndexOffset) % halfPlaneAmount;
                final HalfPlane other = sorted.get(otherIndex);

                if (otherIndexOffset == 1 && twoPiAngle(current.getDirection()) <= twoPiAngle(other.getDirection().rotateByQuarterCircle(2))) {
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
            }
        }

        filterHalfPlanes(sorted, necessities);

        resolved = coordinates;
    }

    @Data
    private class Connection {
        private int prev;
        private int next;
    }

    /**
     * Filter the {@link HalfPlane}s on whether or not they are necessary for the polyhedron.
     *
     * @param halfPlanes the list of half planes
     * @param necessary  the list of necessities
     */
    private void filterHalfPlanes(List<HalfPlane> halfPlanes, List<Boolean> necessary) {
        if (halfPlanes.size() != necessary.size()) {
            return;
        }
        this.halfPlanes = IntStream.range(0, necessary.size())
                .filter(necessary::get)
                .mapToObj(halfPlanes::get)
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

    public Geometry getGeometry(boolean checkBound) {
        final Coordinate[] coordinates = resolved.toArray(new Coordinate[0]);
        final GeometryFactory factory = JTSUtils.GEOMETRY_FACTORY;

        if (!checkBound || isBound()) {
            return factory.createPolygon(coordinates).norm();
        }

        return factory.createLineString(coordinates).norm();
    }

    public List<Ray> getRays() {
        if (isBound()) {
            return new ArrayList<>();
        }

        final ArrayList<Ray> rays = new ArrayList<>();

        halfPlanes.forEach(halfPlane -> {
            final List<Coordinate> cutCoordinates = resolved.stream()
                    .filter(halfPlane::inLine)
                    .collect(Collectors.toList());
            if (cutCoordinates.size() != 1) {
                return;
            }
            final Coordinate p0 = cutCoordinates.get(0);
            Vector2D direction = halfPlane.getDirection();
            Coordinate p1 = direction.translate(p0);
            if (!inside(p1)) {
                p1 = direction.rotateByQuarterCircle(2).translate(p0);
            }
            rays.add(new Ray(p0, p1));
        });

        return rays;
    }

}
