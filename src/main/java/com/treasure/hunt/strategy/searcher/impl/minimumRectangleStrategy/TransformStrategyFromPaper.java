package com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;

/**
 * @author bsen
 */
public class TransformStrategyFromPaper {
    private Point searcherStartPosition;
    private AffineTransformation fromPaper;
    private AffineTransformation forPaper;
    private AffineTransformation fromPaperWithStartPointDisplacement;

    public TransformStrategyFromPaper(HalfPlaneHint hint, Point searcherStartPosition) {
        this.searcherStartPosition = searcherStartPosition;

        double radius = hint.getCenter().distance(hint.getRight());
        double sinHintAngle = (hint.getRight().y - hint.getCenter().y) / radius;
        double cosHintAngle = (hint.getRight().x - hint.getCenter().x) / radius;
        fromPaper = AffineTransformation.rotationInstance(sinHintAngle, cosHintAngle);

        double sinHintAngleReverse = (hint.getCenter().y - hint.getRight().y) / radius;
        double cosHintAngleReverse = (hint.getRight().x - hint.getCenter().x) / radius;
        forPaper = AffineTransformation.rotationInstance(sinHintAngleReverse, cosHintAngleReverse);

        AffineTransformation displacementFromPaper = AffineTransformation.shearInstance(searcherStartPosition.getX(),
                searcherStartPosition.getY());
        fromPaperWithStartPointDisplacement = new AffineTransformation(fromPaper);
        fromPaperWithStartPointDisplacement.compose(displacementFromPaper);

    }

    Coordinate transformForPaper(Coordinate c) {
        return forPaper.transform(new Coordinate(
                c.x - searcherStartPosition.getX(),
                c.y - searcherStartPosition.getY()), new Coordinate());
    }

    Point transformForPaper(Point p) {
        return JTSUtils.GEOMETRY_FACTORY.createPoint(transformForPaper(p.getCoordinate()));
    }

    HalfPlaneHint transformForPaper(HalfPlaneHint hint) {
        return new HalfPlaneHint(transformForPaper(hint.getCenter()), transformForPaper(hint.getRight()));
    }

    Coordinate transformFromPaper(Coordinate c) {
        Coordinate transformedC = new Coordinate();
        fromPaper.transform(c, transformedC);
        transformedC.setX(transformedC.getX() + searcherStartPosition.getX());
        transformedC.setY(transformedC.getY() + searcherStartPosition.getY());
        return transformedC;
    }

    HalfPlaneHint transformFromPaper(HalfPlaneHint hint) {
        return new HalfPlaneHint(transformFromPaper(hint.getCenter()), transformFromPaper(hint.getRight()));
    }

    Polygon transformFromPaper(Polygon polygon) {
        return (Polygon) fromPaperWithStartPointDisplacement.transform(polygon);
    }

    Point transformFromPaper(Point point) {
        return JTSUtils.GEOMETRY_FACTORY.createPoint(transformFromPaper(point.getCoordinate()));
    }

    Coordinate[] transformFromPaper(Coordinate[] coordinates) {
        Coordinate[] result = new Coordinate[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            result[i] = transformFromPaper(coordinates[i]);
        }
        return result;
    }

    SearchPath transformFromPaper(SearchPath move) {
        SearchPath outputMove = new SearchPath();
        for (Point wayPoint : move.getPoints()) {
            outputMove.addPoint(transformFromPaper(wayPoint));
        }
        return outputMove;
    }
}
