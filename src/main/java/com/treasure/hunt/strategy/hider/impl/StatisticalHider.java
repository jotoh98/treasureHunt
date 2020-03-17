package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.AffineTransformation;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class StatisticalHider{
    protected GameField gameField;
    protected GeometryFactory gf = JTSUtils.GEOMETRY_FACTORY;

    protected Point startingPoint;
    protected Geometry currentPossibleArea;

    protected double centroidDistanceToTreasure;
    protected double absoluteAreaCutoff;
    protected double relativeAreaCutoff;
    protected double counterStrategyGeometryCutoff;
    protected double distanceFromNormalAngleRay;

    protected double preferredHintSize = 90;

    public void init(Point searcherStartPosition) {
        this.gameField = new GameField();
        startingPoint = searcherStartPosition;
        Point initialTreasureLocation = getTreasureLocation();

        gameField.init(searcherStartPosition, initialTreasureLocation);
        this.currentPossibleArea = gameField.getPossibleArea();
    }

    public AngleHint move(SearchPath searchPath) {

        // update GameField
        gameField.commitPlayerMovement(searchPath);

        // generate Hints
        List<AngleHint> possibleHints = generateHints(360, searchPath.getLastPoint() );

        // evaluateHints --> use the GameField
        AngleHint hint = eval(possibleHints);

        // commitHint
        gameField.commitHint(hint);
        this.currentPossibleArea = gameField.getPossibleArea();
        // return Hint
        return hint;

    }


    public abstract AngleHint eval( List<AngleHint> hints);

    /**
     * Generates {samples} evenly spaced angles
     *
     * @param samples determines how many Hints are returned
     * @return
     */
    private List<AngleHint> generateHints(int samples, Point hintCenter) {

        final double twoPi = Math.PI * 2;

        AngleHint hint;
        List<AngleHint> hints = new ArrayList<>();

        // if player out of bounding area use the line orthogonal to the line from player to boundingArea Center
        if (!gameField.isWithinGameField(hintCenter)) {
            log.info("player not in bounding circle, giving generic hint");
            // use translation then rotation on Player Point by 90degree then translate back
            //AffineTransform orthAroundPlayer = new AffineTransform(0.0, -1.0 , 2 * hintCenter.getX() , 1.0 , 0.0, -hintCenter.getX() + hintCenter.getY());
            AffineTransformation orthAroundPlayer = new AffineTransformation();
            orthAroundPlayer.rotate(Math.toRadians(90), hintCenter.getX(), hintCenter.getY());
            Coordinate orth = startingPoint.getCoordinate().copy(); //line to middle of circle
            orthAroundPlayer.transform(orth, orth);

            Point right = gf.createPoint(new Coordinate(hintCenter.getX() - (orth.x - hintCenter.getX()), hintCenter.getY() - (orth.y - hintCenter.getY())));
            Point left = gf.createPoint(orth);

            AngleHint outOfBoundsHint = new AngleHint(right.getCoordinate(), hintCenter.getCoordinate(), left.getCoordinate());
            hints.add(outOfBoundsHint);


        } else {

            for (int i = 0; i < samples; i++) {
                double rightAngle = twoPi * (((double) i) / samples);
                double leftAngle = rightAngle + Angle.toRadians(preferredHintSize);
                log.info("");
                double dX_right = Math.cos(rightAngle);
                double dY_right = Math.sin(rightAngle);
                double dX_left = Math.cos(leftAngle);
                double dY_left = Math.sin(leftAngle);
                Point right = gf.createPoint(new Coordinate(hintCenter.getX() + dX_right, hintCenter.getY() + dY_right));
                Point left = gf.createPoint(new Coordinate(hintCenter.getX() + dX_left, hintCenter.getY() + dY_left));

                hint = new AngleHint(right.getCoordinate(), hintCenter.getCoordinate(), left.getCoordinate());
                log.info("ANGLESIZE: " +  Angle.toDegrees(hint.getGeometryAngle().extend()));
                hints.add(hint);
            }
        }
        return hints;
    }

    public abstract Point getTreasureLocation();
}
