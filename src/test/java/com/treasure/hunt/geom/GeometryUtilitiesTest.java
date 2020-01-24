package com.treasure.hunt.geom;

import com.treasure.hunt.jts.geom.Line;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class GeometryUtilitiesTest {

    @Test
    void linesIntersect() {
        Line l1 = new Line(0, 0, 0, 1);
        Line l2 = new Line(1, 1, 2, 0);
        assertNotNull(l1.lineIntersection(l2));
    }

}