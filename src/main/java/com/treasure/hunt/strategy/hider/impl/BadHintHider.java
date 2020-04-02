package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.strategy.searcher.impl.strategyFromPaper.StrategyFromPaper;
import com.treasure.hunt.utils.EventBusUtils;
import com.treasure.hunt.utils.JTSUtils;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
public class BadHintHider implements Hider<HalfPlaneHint> {

    private Point treasure;
    private GeometryFactory GEOMETRY_FACTORY = JTSUtils.GEOMETRY_FACTORY;
    @Override
    public void init(final Point searcherStartPosition) {
        treasure = JTSUtils.shuffleTreasure();

    }

    @Override
    public HalfPlaneHint move(final SearchPath searchPath) {
        Point playerPosition = searchPath.getLastPoint();
        List<GeometryItem<?>> geometryItems = searchPath.getAdditional();

        Polygon currentRectangle;

        try {

            GeometryItem<Polygon>  currentRectangleGeometryItem=  (GeometryItem<Polygon>) geometryItems.stream().filter( item -> item.getGeometryType().getDisplayName().equals("current rectangle")).findFirst().get();
            currentRectangle = currentRectangleGeometryItem.getObject();

        }catch (NoSuchElementException e){
            EventBusUtils.LOG_LABEL_EVENT.trigger(getClass().getSimpleName() + " didn't find current rectangle. Are you playing against " + StrategyFromPaper.class.getSimpleName() + "?");
            log.trace("generic rectangle");
            // else give a 45degree / 135degree Hint relative to x-axis
            Coordinate[] rectangleCoords = new Coordinate[5];
            rectangleCoords[0] = new Coordinate(-10000, 10000);
            rectangleCoords[1] = new Coordinate( 10000, 10000);
            rectangleCoords[2] = new Coordinate( 10000, -10000);
            rectangleCoords[3] = new Coordinate(-10000, -10000);
            rectangleCoords[4] = new Coordinate(-10000, 10000);
            currentRectangle = GEOMETRY_FACTORY.createPolygon(rectangleCoords);

        }
            // quick sanity check
            log.trace("is rectangle?" +  currentRectangle.isRectangle());

            Coordinate topLeft = currentRectangle.getCoordinates()[0].copy();
            topLeft.y = topLeft.y -0.1;
            Coordinate bottomRight = currentRectangle.getCoordinates()[2].copy();
            bottomRight.y = bottomRight.y + 0.1;

            for(Coordinate c : currentRectangle.getCoordinates()){
                log.trace(c.toString());
            }


            // there's 2 possible Hint now, only one will contain the treasure


            HalfPlaneHint badHint = new HalfPlaneHint(playerPosition.getCoordinate(), bottomRight); // upward right facing

            log.trace("the up right facing Hint contains treasure: " + badHint.getGeometryAngle().inView(treasure.getCoordinate()));
        if (!badHint.getGeometryAngle().inView(treasure.getCoordinate())) {

            badHint = new HalfPlaneHint(playerPosition.getCoordinate(),bottomRight); // bottom left facing
            log.trace("the bottom left facing Hint contains treasure: " + badHint.getGeometryAngle().inView(treasure.getCoordinate()));


            // strategyFromPaper is currently performing a scan or the 2-distance step away from the centroid
            if (!badHint.getGeometryAngle().inView(treasure.getCoordinate())) {

                log.info("can't give diagonal Hint, returning generic 45degree one");

                badHint = new HalfPlaneHint(playerPosition.getCoordinate(), new Coordinate( playerPosition.getX()+1, playerPosition.getY()-1)); //up right first again

                if (!badHint.getGeometryAngle().inView(treasure.getCoordinate())){
                    badHint = new HalfPlaneHint(playerPosition.getCoordinate(), new Coordinate( playerPosition.getX()-1, playerPosition.getY() + 1)); //else bottom left
                }
            }

        }
        log.info("given Hint" + badHint.getGeometryAngle().toString());
        return badHint;
    }

    @Override
    public Point getTreasureLocation() {
        return treasure;
    }
}
