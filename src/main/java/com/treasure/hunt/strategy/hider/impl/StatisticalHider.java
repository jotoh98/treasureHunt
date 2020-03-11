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
public abstract class StatisticalHider implements Hider<AngleHint> {
    protected GameField gameField;
    protected GeometryFactory gf = JTSUtils.GEOMETRY_FACTORY;

    protected Point startingPoint;
    protected Geometry currentPossibleArea;

    protected double centroidDistanceToTreasure;
    protected double absoluteAreaCutoff;
    protected double relativeAreaCutoff;
    protected double counterStrategyGeometryCutoff;
    protected double distanceFromNormalAngleRay;

    @Override
    public void init(Point searcherStartPosition) {
        this.gameField = new GameField();
        startingPoint = searcherStartPosition;
        Point initialTreasureLocation = getTreasureLocation();

        gameField.init(searcherStartPosition, initialTreasureLocation);
        this.currentPossibleArea = gameField.getPossibleArea();
    }

    @Override
    public AngleHint move(SearchPath searchPath) {

        // update GameField
        gameField.commitPlayerMovement(searchPath);

        // generate Hints
        List<AngleHint> possibleHints = generateHints(360, searchPath.getLastPoint() );

        // evaluateHints --> use the GameField
        AngleHint hint = eval(possibleHints);


        // commitHint
        gameField.commitHint(hint);
        // return Hint
        return hint;

    }

    // PROPER DOING !!
    public abstract AngleHint eval( List<AngleHint> hints);/*{

        List<Pair<Coordinate, Double>> interestPoints = getWorstPointsOnAllEdges();
        Pair<Coordinate, Double> maxPoint = interestPoints.get(0);

        this.favoredTreasureLocation = new GeometryItem<>(gf.createPoint(maxPoint.getKey()), GeometryType.WORST_CONSTANT);
        log.info("Checking possible Hints for containment of " + this.favoredTreasureLocation.getObject());

        double areaBeforeHint = this.possibleArea.getObject().getArea();
        HintEvaluator evaluator = HintEvaluator.initRound(this.currentPlayersPosition.getCoordinate(), areaBeforeHint);


       !!!!!!           List<AngleHintStat> hintStats = new ArrayList<>();              !!!!!


        for (Pair<Coordinate, Double> p : interestPoints) {
            evaluator.registerPointOfInterest(p);
        }

        AngleHint evaluatedHint = evaluator.evaluateRound();
        log.trace(" the evalHint " + Angle.toDegrees(evaluatedHint.getGeometryAngle().getNormalizedAngle()));
        log.trace(" the max Angle " + Angle.toDegrees(maxAngle.getGeometryAngle().getNormalizedAngle()));
//        log.trace(" the maxGeometry " + maxGeometry);
//        log.trace(" the worst Point " + this.favoredTreasureLocation.getObject());
//        log.trace(" the maxGeometry covers: " + maxGeometry.buffer(0.0001).covers(favoredTreasureLocation.getObject()));
        this.possibleArea = new GeometryItem<>(maxGeometry, GeometryType.POSSIBLE_TREASURE, possibleAreaStyle);
        log.info(possibleArea.getObject().toString());

    }*/



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
                double angle = twoPi * (((double) i) / samples);
                double dX = Math.cos(angle);
                double dY = Math.sin(angle);
                Point right = gf.createPoint(new Coordinate(hintCenter.getX() + dX, hintCenter.getY() + dY));
                Point left = gf.createPoint(new Coordinate(hintCenter.getX() - dX, hintCenter.getY() - dY));

                hint = new AngleHint(right.getCoordinate(), hintCenter.getCoordinate(), left.getCoordinate());
                hints.add(hint);
            }
        }
        return hints;
    }


    @Override
    public abstract Point getTreasureLocation();
}
