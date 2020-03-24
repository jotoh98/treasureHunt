package com.treasure.hunt.jts.geom;

import com.treasure.hunt.utils.JTSUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

class RayTest {

    private final Ray testRay = new Ray(new Coordinate(0, 0), new Coordinate(.1, 0));

    private final LineSegment segmentIntersection = new LineSegment(new Coordinate(1, 0), new Coordinate(1, 1));
    private final LineSegment segmentNoIntersection = new LineSegment(new Coordinate(1, 1), new Coordinate(2, 1));

    private final Line lineIntersection = new Line(new Coordinate(1, -2), new Coordinate(1, -3));
    private final Line lineNoIntersection = new Line(new Coordinate(0, -1), new Coordinate(-1, 0));

    private final Ray rayIntersection = new Ray(new Coordinate(1, 5), new Coordinate(1, 4));
    private final Ray rayNoIntersection = new Ray(new Coordinate(0, -1), new Coordinate(-1, 0));

    @Test
    void testIntersection() {
        Coordinate segment = testRay.intersection(segmentIntersection);
        assertIntersects(segment);

        Coordinate segmentNo = testRay.intersection(segmentNoIntersection);
        Assertions.assertNull(segmentNo);

        Coordinate line = testRay.intersection(lineIntersection);
        assertIntersects(line);

        Coordinate lineNo = testRay.intersection(lineNoIntersection);
        Assertions.assertNull(lineNo);

        Coordinate ray = testRay.intersection(rayIntersection);
        assertIntersects(ray);

        Coordinate rayNo = testRay.intersection(rayNoIntersection);
        Assertions.assertNull(rayNo);
    }

    private void assertIntersects(Coordinate intersection) {
        Assertions.assertNotNull(intersection);
        Assertions.assertTrue(JTSUtils.doubleEqual(intersection.distance(new Coordinate(1, 0)), 0));
    }

    @Test
    void testInside() {
        Assertions.assertTrue(testRay.inRay(new Coordinate(400, 0)));
        Assertions.assertTrue(testRay.inRay(new Coordinate(0, 0)));
        Assertions.assertFalse(testRay.inRay(new Coordinate(-1, 0)));
        Assertions.assertTrue(testRay.inLine(new Coordinate(-1, 0)));
    }
}