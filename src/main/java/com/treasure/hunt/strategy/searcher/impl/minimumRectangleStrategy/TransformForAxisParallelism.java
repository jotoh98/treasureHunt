package com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPathPrototype;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;

/**
 * @author Rank
 */
public class TransformForAxisParallelism {
    private Point internalCenterInExternalRepresentation;
    private AffineTransformation toExternal;
    private AffineTransformation toInternal;
    private AffineTransformation toExternalWithStartPointDisplacement;
    private AffineTransformation toInternalWithStartPointDisplacement;

    /**
     * Creates a transformer where the HalfPlaneHint hint in internal coordinates is parallel to the x-axis and shows
     * upwards and the internalCenterInExternalRepresentation is the point (0,0) in internal coordinates.
     *
     * @param hint
     * @param internalCenterInExternalRepresentation
     */
    public TransformForAxisParallelism(HalfPlaneHint hint, Point internalCenterInExternalRepresentation) {
        this.internalCenterInExternalRepresentation = internalCenterInExternalRepresentation;
        generateTransformations(hint.getCenter(), hint.getRight());
    }

    /**
     * Creates a transformer where the line line in internal coordinate is parallel to the x-axis and
     * the point line.p0 is (0,0) in internal coordinates.
     */
    public TransformForAxisParallelism(LineSegment line) {
        this.internalCenterInExternalRepresentation = JTSUtils.GEOMETRY_FACTORY.createPoint(line.p0);
        generateTransformations(line.p0, line.p1);
    }

    private void generateTransformations(Coordinate left, Coordinate right) {
        double radius = left.distance(right);
        double sinHintAngle = (right.y - left.y) / radius;
        double cosHintAngle = (right.x - left.x) / radius;
        toExternal = AffineTransformation.rotationInstance(sinHintAngle, cosHintAngle);

        double sinHintAngleReverse = (left.y - right.y) / radius;
        double cosHintAngleReverse = (right.x - left.x) / radius;
        toInternal = AffineTransformation.rotationInstance(sinHintAngleReverse, cosHintAngleReverse);

        AffineTransformation displacementFromCenter = AffineTransformation.translationInstance(
                internalCenterInExternalRepresentation.getX(), internalCenterInExternalRepresentation.getY());
        toExternalWithStartPointDisplacement = new AffineTransformation(toExternal);
        toExternalWithStartPointDisplacement.compose(displacementFromCenter);

        AffineTransformation negativeDisplacementFromCenter = AffineTransformation.translationInstance(
                -internalCenterInExternalRepresentation.getX(), -internalCenterInExternalRepresentation.getY());
        toInternalWithStartPointDisplacement = new AffineTransformation(toInternal);
        toInternalWithStartPointDisplacement.composeBefore(negativeDisplacementFromCenter);
    }

    Coordinate toInternal(Coordinate c) {
        return toInternal.transform(new Coordinate(
                c.x - internalCenterInExternalRepresentation.getX(),
                c.y - internalCenterInExternalRepresentation.getY()), new Coordinate());
    }

    Point toInternal(Point p) {
        return JTSUtils.GEOMETRY_FACTORY.createPoint(toInternal(p.getCoordinate()));
    }

    HalfPlaneHint toInternal(HalfPlaneHint hint) {
        return new HalfPlaneHint(toInternal(hint.getCenter()), toInternal(hint.getRight()));
    }

    Polygon toInternal(Polygon polygon) {
        return (Polygon) toInternalWithStartPointDisplacement.transform(polygon);
    }

    Coordinate toExternal(Coordinate c) {
        Coordinate transformedC = new Coordinate();
        toExternal.transform(c, transformedC);
        transformedC.setX(transformedC.getX() + internalCenterInExternalRepresentation.getX());
        transformedC.setY(transformedC.getY() + internalCenterInExternalRepresentation.getY());
        return transformedC;
    }

    HalfPlaneHint toExternal(HalfPlaneHint hint) {
        return new HalfPlaneHint(toExternal(hint.getCenter()), toExternal(hint.getRight()));
    }

    Polygon toExternal(Polygon polygon) {
        return (Polygon) toExternalWithStartPointDisplacement.transform(polygon);
    }

    Point toExternal(Point point) {
        return JTSUtils.GEOMETRY_FACTORY.createPoint(toExternal(point.getCoordinate()));
    }

    Coordinate[] toExternal(Coordinate[] coordinates) {
        Coordinate[] result = new Coordinate[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            result[i] = toExternal(coordinates[i]);
        }
        return result;
    }

    SearchPathPrototype toExternal(SearchPathPrototype move) {
        SearchPathPrototype outputMove = new SearchPathPrototype();
        for (Point wayPoint : move.getPoints()) {
            outputMove.addPoint(toExternal(wayPoint));
        }
        return outputMove;
    }
}
