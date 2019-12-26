package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.geom.GeometryAngle;
import com.treasure.hunt.geom.Line;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Here is an angle searcher which will remember all given hints.
 * The searcher works in phases in which it searches the square positioned at the starting position, of size 2^phase.
 * After each phase the old hints are reused.
 * The square will be shrunken to a polygon of which all the given hints are subtracted.
 * Then, if the Polygon contains less than say 1/4 of the Square, the next phase begins.
 *
 * Currently, the strategy is to simply go to the centroid of the polygon and then get a new hint.
 *
 *
 * @author Vincent Schönbach
 */
public class IntelligentAngleSearcher implements Searcher<AngleHint> {
    private Coordinate location;

    private Movement nextMoves;
    private AngleHint currentHint;

    private List<GeometryAngle> hints = new ArrayList<>();

    private Polygon area;
    private GeometryItem<Geometry> lastAreaDisplayItem;
    private GeometryItem<Geometry> lastRectangleDisplayItem;
    private int phase;

    @Override
    public void init(Point startPosition) {
        this.location = startPosition.getCoordinate();
        nextMoves = new Movement(JTSUtils.GEOMETRY_FACTORY.createPoint(location));

        phase = 0;
        nextPhase();
        }

    @Override
    public Movement move() {
        gotoPoint(location);
        return nextMoves;
    }

    @Override
    public Movement move(AngleHint angleHint) {
        nextMoves = new Movement(JTSUtils.GEOMETRY_FACTORY.createPoint(location));
        currentHint = angleHint;
        hints.add(currentHint.getGeometryAngle().copy());
        handleHint(currentHint.getGeometryAngle());

        if (area.getArea() < getAreaRectangle(phase).getArea()/4
            && !getAreaRectangle(phase).getBoundary().disjoint(area))
        {
            nextPhase();
        }

        gotoPoint(area.getCentroid().getCoordinate());

        drawArea();
        return nextMoves;
    }

    private void handleHint(GeometryAngle ang){
        //Split Area-Polygon by Hint
        // by Creating Splitting-Polygon to perform intersection

        GeometryAngle angle = ang.copy();

        double outsideOfAreaDistance = Math.pow(2, phase+5);
        Coordinate pointOutsideAreaLeft = JTSUtils.coordinateInDistance(angle.getCenter(), angle.getLeft(), outsideOfAreaDistance);
        Coordinate pointOutsideAreaRight = JTSUtils.coordinateInDistance(angle.getCenter(), angle.getRight(), outsideOfAreaDistance);

        Polygon biggerArea = getAreaRectangle(phase+1);
        List<LineSegment> lineSegs = new ArrayList<>();
        Coordinate[] coords = biggerArea.getCoordinates();//A B C D A
        for (int i = 0; i < coords.length-1; i++){
            lineSegs.add(new LineSegment(coords[i].copy(), coords[i+1].copy()));
        }
        LineSegment l = new LineSegment(angle.getCenter().copy(), pointOutsideAreaLeft);
        LineSegment r = new LineSegment(angle.getCenter().copy(), pointOutsideAreaRight);
        for (int i = 0; i < lineSegs.size(); i++){
            Coordinate p = l.intersection(lineSegs.get(i));
            if (p != null)
            {
                lineSegs.add(i+1, new LineSegment(angle.getCenter().copy(), p));
                lineSegs.add(i+2, new LineSegment(p, lineSegs.get(i).p1));
                Collections.rotate(lineSegs, -(i+1));
                break;
            }
        }

        for (int i = 1; i < lineSegs.size(); i++){
            Coordinate p = r.intersection(lineSegs.get(i));
            if (p != null) {
                lineSegs.get(i).setCoordinates(lineSegs.get(i).p0, p);
                lineSegs.add(i + 1, new LineSegment(p, angle.getCenter().copy()));
                lineSegs.subList(i + 2, lineSegs.size()).clear();
                break;
            }
        }

        Coordinate[] coords2 = new Coordinate[lineSegs.size()+1];
        for (int i = 0; i < coords2.length-1; i++) {
            coords2[i] = lineSegs.get(i).p0;
        }
        coords2[coords2.length-1] = coords2[0];

        Polygon splittingPolygon = new Polygon(new LinearRing(new CoordinateArraySequence(coords2),
                JTSUtils.GEOMETRY_FACTORY), null, JTSUtils.GEOMETRY_FACTORY);

        // now adjust area
        if (area.intersects(splittingPolygon))
        {
            Geometry areaGeo = area.intersection(splittingPolygon);
            if (areaGeo instanceof Polygon) {
                area = (Polygon) areaGeo;
            }
        }
    }

    private void nextPhase(){
        phase++;
        area = getAreaRectangle(phase);
        for (GeometryAngle h : this.hints){ //replay all hints for bigger area
            handleHint(h);
        }
        drawArea();

        //Draw Rectangle of the Phase
        if (lastRectangleDisplayItem != null)
            nextMoves.getToBeRemoved().add(lastRectangleDisplayItem);
        lastRectangleDisplayItem = new GeometryItem<>(getAreaRectangle(phase), GeometryType.SEARCH_RECTANGLE_BOUNDING);
        nextMoves.addAdditionalItem(lastRectangleDisplayItem);

    }

    private void drawArea(){
        if (lastAreaDisplayItem != null)
            nextMoves.getToBeRemoved().add(lastAreaDisplayItem);
        lastAreaDisplayItem = new GeometryItem<>(area, GeometryType.SEARCH_BOUNDING);
        nextMoves.addAdditionalItem(lastAreaDisplayItem);
    }

    private Polygon getAreaRectangle(int i){
        int length = (int) Math.pow(2, i);
        Coordinate A = new Coordinate(-length/2., length/2.);
        Coordinate B = new Coordinate(length/2., length/2.);
        Coordinate C = new Coordinate(length/2., -length/2.);
        Coordinate D = new Coordinate(-length/2., -length/2.);
        return new Polygon(new LinearRing(new CoordinateArraySequence(new Coordinate[]{A, B, C, D, A.copy()}),
                JTSUtils.GEOMETRY_FACTORY), null, JTSUtils.GEOMETRY_FACTORY);
    }

    private void gotoPoint(Coordinate c){
        nextMoves.addWayPoint(JTSUtils.GEOMETRY_FACTORY.createPoint(c));
        Coordinate[] coordinates = {location, c};
        nextMoves.addAdditionalItem(new GeometryItem<>(new LineString(new CoordinateArraySequence(coordinates),
                JTSUtils.GEOMETRY_FACTORY), GeometryType.SEARCHER_MOVEMENT));
        location = c.copy();
    }
}
