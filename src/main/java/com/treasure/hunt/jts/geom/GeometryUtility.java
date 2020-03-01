package com.treasure.hunt.jts.geom;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;

import java.util.Arrays;

/**
 * @author jotoh
 */
public abstract class GeometryUtility extends Geometry implements Shapeable {

    Coordinate[] coordinates;

    /**
     * Creates a new <code>Geometry</code> via the specified GeometryFactory.
     *
     * @param factory
     */
    GeometryUtility(GeometryFactory factory) {
        super(factory);
    }

    GeometryUtility(GeometryFactory factory, Coordinate[] coordinates) {
        super(factory);
        this.coordinates = coordinates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGeometryType() {
        return "GeometryUtility";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSimple() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumPoints() {
        return coordinates.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDimension() {
        return 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBoundaryDimension() {
        return getDimension();
    }

    /**
     * @param n the index of the {@link Coordinate}, we want from the coordinate array, stored in {@code this}.
     * @return the {@code n}-th {@link Coordinate}, stored in {@code this}.
     */
    public Coordinate getCoordinate(int n) {
        return coordinates[n];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Coordinate getCoordinate() {
        return getCoordinate(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Coordinate[] getCoordinates() {
        return coordinates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return !Arrays.stream(coordinates).anyMatch(a ->
                Arrays.stream(coordinates).anyMatch(a::equals)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equalsExact(Geometry other, double tolerance) {
        return Arrays.stream(coordinates).allMatch(a ->
                Arrays.stream(other.getCoordinates()).allMatch(b -> equal(a, b, tolerance))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(CoordinateFilter filter) {
        for (Coordinate coordinate : coordinates) {
            filter.filter(coordinate);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(CoordinateSequenceFilter filter) {
        //TODO: necessary
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(GeometryFilter filter) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(GeometryComponentFilter filter) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Envelope computeEnvelopeInternal() {
        double xMax = Double.NEGATIVE_INFINITY;
        double xMin = Double.POSITIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;

        for (int i = 0; i < getNumPoints(); i++) {
            double x = coordinates[i].getX();
            double y = coordinates[i].getX();
            if (x > xMax) {
                xMax = x;
            }
            if (x < xMin) {
                xMin = x;
            }
            if (y > yMax) {
                yMax = y;
            }
            if (y < yMin) {
                yMin = y;
            }
        }

        return new Envelope(xMax, xMin, yMax, yMin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int compareToSameClass(Object o) {
        GeometryUtility other = (GeometryUtility) o;

        int i = 0;
        int j = 0;
        while (i < getNumPoints() && j < other.getNumPoints()) {
            int comparison = getCoordinate(i).compareTo(other.getCoordinate(j));
            if (comparison != 0) {
                return comparison;
            }
            i++;
            j++;
        }
        if (i < getNumPoints()) {
            return 1;
        }
        if (j < other.getNumPoints()) {
            return -1;
        }
        return 0;
    }

    protected int compareToSameClass(Object o, CoordinateSequenceComparator comp) {
        GeometryUtility other = (GeometryUtility) o;
        return comp.compare(coordinates, other.coordinates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void normalize() {
        if (coordinates.length < 1) {
            return;
        }

        for (int i = 1; i < getNumPoints(); i++) {
            Vector2D vector2D = new Vector2D(getCoordinate(0), getCoordinate(i));
            coordinates[i] = vector2D.normalize().translate(getCoordinate(0));
        }
    }
}
