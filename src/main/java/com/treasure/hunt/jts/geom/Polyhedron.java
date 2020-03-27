package com.treasure.hunt.jts.geom;

import com.treasure.hunt.utils.JTSUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Polyhedron defined by a list of {@link HalfPlane}s.
 */
@Slf4j
@NoArgsConstructor
public class Polyhedron {

    /**
     * List of halfPlanes defining the polyhedron.
     */
    private List<HalfPlane> halfPlanes = new ArrayList<>();

    /**
     * Convex hull of the (potentially unbound) polyhedron.
     * By default extend the polyhedron 10 length units into the unbound region.
     * This extension factor can be manipulated using {@link #unboundExtend}.
     */
    @Getter
    private Geometry convexHull = JTSUtils.GEOMETRY_FACTORY.createGeometryCollection();

    /**
     * Factor of the extend of the unbound section.
     */
    @Setter
    private double unboundExtend = 10d;

    /**
     * Adds a half plane to the polyhedron.
     * Automatically resolves the vertex-representation and filters out degenerate half planes.
     *
     * @param halfPlane half plane to be added
     * @see #resolve()
     * @see #filterDegenerate()
     */
    public void addHalfPlane(HalfPlane halfPlane) {
        halfPlanes.add(halfPlane);
        resolve();
        filterDegenerate();
    }

    /**
     * Resolve the vertex-representation from the half-plane representation.
     * We compute naively all of the intersections filtering them on whether they are inside.
     * Also, we add two unbound section coordinates.
     */
    public void resolve() {
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        int size = halfPlanes.size();
        for (int i = 0; i < size; i++) {
            for (int j = 1; j < size; j++) {
                HalfPlane first = halfPlanes.get(i);
                HalfPlane second = halfPlanes.get((i + j) % size);

                Coordinate intersection = first.intersection(second);

                if (intersection != null && inside(intersection)) {
                    coordinates.add(intersection);

                    resolveUnbound(coordinates, first, intersection);
                    resolveUnbound(coordinates, second, intersection);
                }
            }
        }
        convexHull = JTSUtils.createConvexHull(coordinates).getConvexHull();
    }

    /**
     * Resolves an unbound coordinate.
     * This coordinate is not a part of the optimal solution vertex set.
     * It just symbolizes the infinite unbound section of the potentially unbound polyhedron.
     *
     * @param coordinates  list of resolved coordinates
     * @param halfPlane    half plane the intersection belongs
     * @param intersection the intersection vertex at the unbound section
     */
    private void resolveUnbound(ArrayList<Coordinate> coordinates, HalfPlane halfPlane, Coordinate intersection) {
        Vector2D firstDirection = halfPlane.getDirection().normalize().multiply(unboundExtend);
        Coordinate firstUnbound = firstDirection.translate(intersection);
        if (!inside(firstUnbound)) {
            firstUnbound = firstDirection.multiply(-1d).translate(intersection);
            if (inside(firstUnbound)) {
                coordinates.add(firstUnbound);
            }
        } else {
            coordinates.add(firstUnbound);
        }
    }

    /**
     * Filters out degenerate half planes.
     * A half plane is degenerate, if it doesn't provide further restriction.
     * We check this by stating that in such a case, a degenerate half plane also does not intersect with
     */
    private void filterDegenerate() {
        List<LineSegment> lineSegments = getLineSegments();

        if (lineSegments.size() == 0) {
            return;
        }

        halfPlanes = halfPlanes.stream()
                .filter(
                        halfPlane -> lineSegments.stream()
                                .anyMatch(lineSegment -> halfPlane.intersection(lineSegment) != null)
                )
                .collect(Collectors.toList());
    }

    /**
     * Adds a {@link GeometryAngle} as two {@link HalfPlane}s to the polyhedron.
     *
     * @param angle geometry angle to add
     */
    public void addAngle(GeometryAngle angle) {
        addHalfPlane(new HalfPlane(angle.getCenter().copy(), angle.getLeft().copy()));
        addHalfPlane(new HalfPlane(angle.getRight().copy(), angle.getCenter().copy()));
    }

    /**
     * Tests, if a {@link Coordinate} lays inside of the polyhedron.
     *
     * @param c tested coordinate
     * @return whether the tested coordinate lays inside of the polyhedron
     */
    public boolean inside(Coordinate c) {
        return halfPlanes.stream().allMatch(halfPlane -> halfPlane.inside(c));
    }

    /**
     * Get the convex hull as a list of {@link LineSegment}s.
     *
     * @return list of line segments for the convex hull
     */
    private List<LineSegment> getLineSegments() {
        if (convexHull.getCoordinates().length < 2) {
            return Collections.emptyList();
        }
        ArrayList<LineSegment> lineSegments = new ArrayList<>();
        Coordinate prev = null;
        for (Coordinate coordinate : convexHull.getCoordinates()) {
            if (prev == null) {
                prev = coordinate;
                continue;
            }
            lineSegments.add(new LineSegment(prev, coordinate));
        }
        return lineSegments;
    }

}
