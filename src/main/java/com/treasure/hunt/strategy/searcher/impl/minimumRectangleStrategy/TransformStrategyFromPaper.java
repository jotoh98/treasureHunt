package com.treasure.hunt.strategy.searcher.impl.minimumRectangleStrategy;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import lombok.Value;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.AffineTransformation;

@Value
public class TransformStrategyFromPaper {
    private AffineTransformation fromPaper;
    private AffineTransformation forPaper;
    Point searcherStartPosition;
    MinimumRectangleStrategy minimumRectangleStrategy;

    Coordinate transformForPaper(double x, double y) {
        return forPaper.transform(new Coordinate(
                x - searcherStartPosition.getX(),
                y - searcherStartPosition.getY()), new Coordinate());
    }

    Coordinate transformForPaper(Coordinate c) {
        return transformForPaper(c.x, c.y);
    }

    Point transformForPaper(Point p) {
        return JTSUtils.GEOMETRY_FACTORY.createPoint(transformForPaper(p.getCoordinate()));
    }

    HalfPlaneHint transformForPaper(HalfPlaneHint hint) {
        HalfPlaneHint lastHint = null;
        if (hint.getLastHint() != null) {
            lastHint = new HalfPlaneHint(
                    forPaper.transform(transformForPaper(hint.getLastHint().getCenter()), new Coordinate()),
                    forPaper.transform(transformForPaper(hint.getLastHint().getRight()), new Coordinate())
            );
        }
        return new HalfPlaneHint(
                forPaper.transform(transformForPaper(hint.getCenter()), new Coordinate()),
                forPaper.transform(transformForPaper(hint.getRight()), new Coordinate()),
                lastHint
        );
    }

    Point transformFromPaper(Point point) {
        return (Point) fromPaper.transform(point);
    }

    Movement transformFromPaper(Movement move) {
        Movement outputMove = new Movement();
        for (GeometryItem<Point> wayPoint : move.getPoints()) {
            outputMove.addWayPoint(transformFromPaper(wayPoint.getObject()));
        }
        //addState was  not called yet
        return minimumRectangleStrategy.moveReturn(outputMove);
    }
}
