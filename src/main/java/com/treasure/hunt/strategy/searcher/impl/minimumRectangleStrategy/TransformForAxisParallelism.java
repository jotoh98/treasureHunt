package com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import lombok.Getter;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.*;
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
    @Getter
    private double angle;

    /**
     * Creates a transformer where the HalfPlaneHint hint in internal coordinates is parallel to the x-axis and shows
     * upwards and the internalCenterInExternalRepresentation is the point (0,0) in internal coordinates.
     *
     * @param hint                                   the internal x-axis is parallel to
     * @param internalCenterInExternalRepresentation the internal center (point (0,0)) in external coordinates
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

        angle = Angle.normalizePositive(Math.atan2(sinHintAngle, cosHintAngle));

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

    Geometry toInternal(Geometry geom) {
        return toInternalWithStartPointDisplacement.transform(geom);
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

    Polygon toExternal(Polygon geom) {
        return (Polygon) toExternalWithStartPointDisplacement.transform(geom);
    }

    Geometry toExternal(Geometry geometry) {
        return toExternalWithStartPointDisplacement.transform(geometry);
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

    SearchPath toExternal(SearchPath move) {
        SearchPath outputMove = new SearchPath();
        for (Point wayPoint : move.getPoints()) {
            outputMove.addPoint(toExternal(wayPoint));
        }
        return outputMove;
    }
}
