package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.AffineTransformation;

/**
 * This Strategy follows normal direction which will be nudged towards the current Hints normal direction
 *
 * @author Ruben Kemna
 */
@Slf4j
public class SteadyDirectionSearcher implements Searcher<AngleHint> {
    private double currentOrientationByAngle = 0.0;
    private double influenceConstant = 0.2;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Point searcherStartPosition) {
    }

    /**
     * @return {@link SearchPath}, which matters no movement.
     */
    @Override
    public SearchPath move() {
        return new SearchPath();
    }

    @Override
    public SearchPath move(AngleHint hint) {
        GeometryAngle geometryHint = hint.getGeometryAngle();
        Coordinate player = geometryHint.getCenter();
        log.info("player: " + hint.getGeometryAngle().getCenter().x + " " + hint.getGeometryAngle().getCenter().y);
        log.info("left: " + hint.getGeometryAngle().getLeft().x + " " + hint.getGeometryAngle().getLeft().y);
        log.info("right: " + hint.getGeometryAngle().getRight().x + " " + hint.getGeometryAngle().getRight().y);

        double angleSize = Angle.interiorAngle(geometryHint.getLeft(), geometryHint.getCenter(), geometryHint.getRight());
        log.info("Hints angleSize" + Angle.toDegrees(angleSize));
        log.info("current normal Angle of WalkingDirection: " + Angle.toDegrees(currentOrientationByAngle));

        Coordinate currentAngleCenter = JTSUtils.middleOfAngleHint(hint);
        double normalizedHintAngle = Angle.angle(player, currentAngleCenter);

        //Create current DirectionPoint
        AffineTransformation currentDirectionPointTransform = new AffineTransformation();
        currentDirectionPointTransform.rotate(currentOrientationByAngle, player.x, player.y);
        Coordinate directionPoint = new Coordinate(player.x + 1, player.y);
        directionPoint = currentDirectionPointTransform.transform(directionPoint, directionPoint);

        //calc the difference of directions
        double distortion = Angle.angleBetweenOriented(directionPoint, player, currentAngleCenter);

        // adjust direction
        AffineTransformation newDirectionTransform = new AffineTransformation();
        newDirectionTransform.rotate(distortion * influenceConstant, player.x, player.y);
        directionPoint = newDirectionTransform.transform(directionPoint, directionPoint);

        this.currentOrientationByAngle = Angle.angle(player, directionPoint);

        log.info("normal Angle of angleCenter: " + Angle.toDegrees(normalizedHintAngle));
        log.info("new normal Angle of WalkingDirection: " + Angle.toDegrees(currentOrientationByAngle));

        Point movePoint = JTSUtils.GEOMETRY_FACTORY.createPoint(directionPoint);

        SearchPath sP = new SearchPath(movePoint);

        return sP;
    }
}
