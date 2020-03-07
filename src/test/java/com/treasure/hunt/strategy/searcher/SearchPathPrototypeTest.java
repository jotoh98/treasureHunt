package com.treasure.hunt.strategy.searcher;

import com.treasure.hunt.RandomNumberArgumentProvider;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.utils.JTSUtils;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author bsen, dorianreineccius
 */
public class SearchPathPrototypeTest {
    @ParameterizedTest
    @ArgumentsSource(RandomNumberArgumentProvider.class)
    public void testGetLines(int randomNumber) {
        SearchPathPrototype searchPathPrototype = new SearchPathPrototype();
        Random random = new Random(randomNumber);
        ArrayList<Point> points = new ArrayList<>();
        Point point1 = JTSUtils.createPoint(random.nextInt(), random.nextInt());
        points.add(point1);
        Point point2 = JTSUtils.createPoint(random.nextInt(), random.nextInt());
        points.add(point2);
        Point point3 = JTSUtils.createPoint(random.nextInt(), random.nextInt());
        points.add(point3);
        Point point4 = JTSUtils.createPoint(random.nextInt(), random.nextInt());
        points.add(point4);
        Point point5 = JTSUtils.createPoint(random.nextInt(), random.nextInt());
        points.add(point5);
        Point point6 = JTSUtils.createPoint(random.nextInt(), random.nextInt());
        points.add(point6);
        Point point7 = JTSUtils.createPoint(random.nextInt(), random.nextInt());
        points.add(point7);
        searchPathPrototype.setPoints(points);
        List<GeometryItem<LineString>> lines = searchPathPrototype.getLinesGeometryItemsList();
        assertLineHasPoint(lines.get(0), point1);
        assertLineHasPoint(lines.get(0), point2);
        assertLineHasPoint(lines.get(1), point2);
        assertLineHasPoint(lines.get(1), point3);
        assertLineHasPoint(lines.get(2), point3);
        assertLineHasPoint(lines.get(2), point4);
        assertLineHasPoint(lines.get(3), point4);
        assertLineHasPoint(lines.get(3), point5);
        assertLineHasPoint(lines.get(4), point5);
        assertLineHasPoint(lines.get(4), point6);
        assertLineHasPoint(lines.get(5), point6);
        assertLineHasPoint(lines.get(5), point7);
    }

    /**
     * This initializes an {@link SearchPathPrototype}, containing one line and tests
     * {@link SearchPathPrototype#getLength()}.
     */
    @Test
    public void getLengthOneLineTest() {
        assertEquals(new SearchPathPrototype(new Coordinate(0, 0), new Coordinate(0, 1)).getLength(), 1);
    }

    /**
     * This initializes an {@link SearchPathPrototype}, containing two lines and tests
     * {@link SearchPathPrototype#getLength()}.
     */
    @Test
    public void getLengthTwoLinesTest() {
        assertEquals(new SearchPathPrototype(new Coordinate(0, 0), new Coordinate(0, 1), new Coordinate(1, 1)).getLength(), 2);
    }

    /**
     * This initializes an {@link SearchPathPrototype}, containing much lines and tests
     * {@link SearchPathPrototype#getLength()}.
     */
    @Test
    public void getLengthMuchLinesTest() {
        assertEquals(new SearchPathPrototype(
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 1),
                new Coordinate(1, 5),
                new Coordinate(-4, 5)
        ).getLength(), 11);
    }

    private void assertLineHasPoint(GeometryItem<LineString> geometryItem, Point point) {
        assertTrue(Arrays.asList(geometryItem.getObject().getCoordinates()).contains(point.getCoordinate()));
    }
}