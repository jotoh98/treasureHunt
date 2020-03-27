package com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.utils.JTSUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

public class ExcludedAreaUtilsTest {
    @BeforeEach
    void setUp() {

    }

    @Test
    public void reduceConvexPolygonTest() {
        Polygon convexPolygon = JTSUtils.GEOMETRY_FACTORY.createPolygon(
                new Coordinate[]{
                        new Coordinate(-1, 1),
                        new Coordinate(1, 1),
                        new Coordinate(1, -1),
                        new Coordinate(-1, -1),
                        new Coordinate(-1, 1)
                }
        );
        HalfPlaneHint halfPlaneHint = new HalfPlaneHint(new Coordinate(0, 0.5), new Coordinate(1, 0.5));
        Polygon result = ExcludedAreasUtils.reduceConvexPolygon(convexPolygon, halfPlaneHint);
        Polygon correctResult = JTSUtils.GEOMETRY_FACTORY.createPolygon(
                new Coordinate[]{
                        new Coordinate(-1, 0.5),
                        new Coordinate(-1, 1),
                        new Coordinate(1, 1),
                        new Coordinate(1, 0.5),
                        new Coordinate(-1, 0.5),
                }
        );
        if (result == null || !result.equalsExact(correctResult)) {
            throw new AssertionError("result:\n " + result + "\nexpected result:\n " + correctResult);
        }
    }

}
