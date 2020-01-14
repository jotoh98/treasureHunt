package com.treasure.hunt.jts.geom;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class PolyhedronTest {

    private static long startTime;

    public static void main(String[] args) {
        final PolyhedronTest polyhedronTest = new PolyhedronTest();
        final Polyhedron polyhedron = polyhedronTest.constructPolyhedron();


    }

    private Coordinate c(double x, double y) {
        return new Coordinate(x, y);
    }

    private HalfPlane hp(Coordinate c1, Coordinate c2) {
        return new HalfPlane(c1, c2);
    }

    private HalfPlane hp(double x0, double y0, double x1, double y1) {
        return hp(c(x0, y0), c(x1, y1));
    }

    public Polyhedron constructPolyhedron() {
        return new Polyhedron(
                hp(0, 0, 1, 0),
                hp(0, 2, 0, 0),
                hp(1, 1, 0, 2),
                hp(1, 0, 1, 1)
        );
    }

    @Test
    void testIntersects() {
        final Polyhedron polyhedron = constructPolyhedron();

        log.info(String.format("%s", polyhedron.getGeometry(false)));
        assertTrue(polyhedron.inside(c(.5, .5)));

        final boolean inside = polyhedron.inside(c(-1, -1));
        assertFalse(inside);
    }

    @Test
    void resolveTest() {
        final Coordinate[] c = {
                c(5.5, 1.66),
                c(2.6, 4.27),
                c(-1.6, 4.57),
                c(-4.56, 0.63),
                c(-2.05, -4.71),
                c(3.58, -3.91)
        };

        final HalfPlane[] hps = new HalfPlane[6];

        for (int i = 0; i < c.length - 1; i++) {
            hps[i] = hp(c[i], c[i + 1]);
        }
        hps[5] = hp(5.68, 6.17, 1.54, 6.37);

        final Polyhedron polyhedron = new Polyhedron(hps);


    }

    @Test
    void runtimeTests() {
        final Random rand = new Random();
        final HalfPlane[] halfPlanes = IntStream.range(0, 1000).mapToObj(i -> hp(
                c(rand.nextDouble() * 10 - 5, rand.nextDouble() * 10 - 5),
                c(rand.nextDouble() * 10 - 5, rand.nextDouble() * 10 - 5))
        ).toArray(HalfPlane[]::new);

        startTimer();
        final Polyhedron polyhedron = new Polyhedron(halfPlanes);
        log.debug(String.format("%s", polyhedron.getGeometry(false)));
        stopTimer();

    }

    private void startTimer() {
        startTime = System.currentTimeMillis();
    }

    private void stopTimer() {
        long end = System.currentTimeMillis();
        log.debug("Run took " + (end - startTime) + " MilliSeconds");
    }
}