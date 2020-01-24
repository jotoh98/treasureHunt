package com.treasure.hunt.jts.geom;

import org.locationtech.jts.geom.*;

/**
 * @author jotoh
 */
public class GeometryLine extends Geometry {
    /**
     * Creates a new <code>Geometry</code> via the specified GeometryFactory.
     *
     * @param factory the factory to construct the line with
     */
    public GeometryLine(GeometryFactory factory) {
        super(factory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGeometryType() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Coordinate getCoordinate() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Coordinate[] getCoordinates() {
        return new Coordinate[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumPoints() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDimension() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Geometry getBoundary() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBoundaryDimension() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Geometry reverse() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equalsExact(Geometry other, double tolerance) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(CoordinateFilter filter) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(CoordinateSequenceFilter filter) {
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
    protected Geometry copyInternal() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void normalize() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Envelope computeEnvelopeInternal() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int compareToSameClass(Object o) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int compareToSameClass(Object o, CoordinateSequenceComparator comp) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getSortIndex() {
        return 0;
    }
}
