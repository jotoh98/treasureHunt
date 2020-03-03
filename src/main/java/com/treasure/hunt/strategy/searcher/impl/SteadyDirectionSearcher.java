package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.AffineTransformation;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * This Strategy follows normal Direction which will be nudged towards the current Hints normal Direction
 *
 */
@Slf4j
public class SteadyDirectionSearcher implements Searcher<AngleHint> {

    private Point startPosition;
    private Point currentPosition;

    private double currentOrientationByAngle;

    private double influenceConstant = 0.2;

    @Override
    public void init(Point searcherStartPosition) {
        this.startPosition = searcherStartPosition;
        this.currentPosition = searcherStartPosition;
        this.currentOrientationByAngle = 0.0;
    }

    @Override
    public Movement move() {
        return new Movement(startPosition);
    }

    @Override
    public Movement move(AngleHint hint) {
        GeometryAngle geometryHint = hint.getGeometryAngle();
        Coordinate player = geometryHint.getCenter();
        log.info("player: " + hint.getGeometryAngle().getCenter().x + " " + hint.getGeometryAngle().getCenter().y);
        log.info("left: " + hint.getGeometryAngle().getLeft().x + " " + hint.getGeometryAngle().getLeft().y);
        log.info("right: " + hint.getGeometryAngle().getRight().x + " " + hint.getGeometryAngle().getRight().y);


        double angleSize = Angle.interiorAngle(geometryHint.getLeft(),geometryHint.getCenter(),geometryHint.getRight());
        log.info("Hints angleSize" + Angle.toDegrees(angleSize));
        log.info("current normal Angle of WalkingDirection: " + Angle.toDegrees(currentOrientationByAngle));

        Coordinate currentAngleCenter = JTSUtils.middleOfAngleHint(hint);
        double normalizedHintAngle = Angle.angle(player, currentAngleCenter);

        //Create current DirectionPoint
        AffineTransformation currentDirectionPointTransform = new AffineTransformation();
        currentDirectionPointTransform.rotate(currentOrientationByAngle , player.x, player.y );
        Coordinate directionPoint = new Coordinate(player.x +1 , player.y);
        directionPoint = currentDirectionPointTransform.transform(directionPoint,directionPoint);

        //calc the difference of directions
        double distortion = Angle.angleBetweenOriented(directionPoint,player,currentAngleCenter);

        // adjust direction
        AffineTransformation newDirectionTransform = new AffineTransformation();
        newDirectionTransform.rotate(distortion * influenceConstant, player.x, player.y);
        directionPoint = newDirectionTransform.transform(directionPoint,directionPoint);


        this.currentOrientationByAngle = Angle.angle(player,directionPoint);


        log.info("normal Angle of angleCenter: " + Angle.toDegrees(normalizedHintAngle));
        log.info("new normal Angle of WalkingDirection: " + Angle.toDegrees(currentOrientationByAngle));


        Point movePoint = JTSUtils.GEOMETRY_FACTORY.createPoint(directionPoint);

        Movement m = new Movement(currentPosition);
        m.addWayPoint(movePoint);
        currentPosition = movePoint;

        return m;
    }
}
