package com.treasure.hunt.utils;

import com.treasure.hunt.jts.geom.Circle;
import com.treasure.hunt.jts.geom.GeometryAngle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Various tests for {@link JTSUtils}.
 *
 * @author Jonathan Hassel
 */
class JTSUtilsTest {

    /**
     * Tests for {@link JTSUtils#validRandomAngle(Coordinate, Coordinate, double)}.
     */
    @Test
    void randomAngleTest() {
        for (int i = 0; i < 100; i++) {
            final Coordinate searcher = new Coordinate(Math.random() * 100, Math.random() * 100);
            final Coordinate treasure = new Coordinate(Math.random() * 100, Math.random() * 100);
            final GeometryAngle angle = JTSUtils.validRandomAngle(searcher, treasure, 2 * Math.PI);
            assertInAngle(angle, treasure);
        }
    }

    /**
     * Tests for {@link JTSUtils#validRandomAngle(Coordinate, Coordinate, double)}.
     */
    @Test
    void invalidRandomAngles() {
        final Coordinate searcher = new Coordinate(Math.random() * 100, Math.random() * 100);
        final Coordinate treasure = new Coordinate(Math.random() * 100, Math.random() * 100);
        final GeometryAngle invalidExtend = JTSUtils.validRandomAngle(searcher, treasure, 0);
        assertNull(invalidExtend);
    }

    /**
     * Tests for {@link JTSUtils#validRandomAngle(Coordinate, Coordinate, double)}.
     */
    @Test
    void treasureAtRandomAngleCenter() {
        final Coordinate sameCoordinate = new Coordinate();
        final GeometryAngle emptyAngle = JTSUtils.validRandomAngle(sameCoordinate, sameCoordinate, 2 * Math.PI);
        assertInAngle(emptyAngle, sameCoordinate);
    }

    /**
     * Tests for {@link JTSUtils#randomContainedCircle(Circle, Coordinate, double)}.
     */
    @Test
    public void innerCircle() {
        for (int i = 0; i < 100; i++) {
            final Circle circle = new Circle(new Coordinate(Math.random() * 100 - 50, Math.random() * 100 - 50), Math.random() * 50.0 + 10.0);
            final Vector2D multiply = Vector2D.create(circle.getRadius(), 0)
                    .rotate(Math.random() * 2 * Math.PI)
                    .multiply(.1 + Math.random() * .9);
            final Coordinate coordinate = multiply.translate(circle.getCenter());
            final Circle circle1 = JTSUtils.randomContainedCircle(circle, coordinate, 1 + Math.random() * (circle.getRadius() - 1));

            if (!circle.covers(circle1)) {
                System.out.println(circle);
                System.out.println(circle1);
            }
            Assertions.assertTrue(circle.inside(coordinate));
            Assertions.assertTrue(circle.covers(circle1));
            Assertions.assertTrue(circle1.inside(coordinate));
        }
    }

    /**
     * @param angle      we want to test, whether {@code coordinate} lies in it
     * @param coordinate {@link Coordinate} we want to test, whether it lies in {@code angle}
     */
    private void assertInAngle(GeometryAngle angle, Coordinate coordinate) {
        assertTrue(JTSUtils.pointInAngle(angle, coordinate));
    }
}
