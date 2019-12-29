package com.treasure.hunt.strategy.searcher.impl.sfp2;

import com.treasure.hunt.geom.GeometryAngle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.strategy.searcher.impl.StrategyFromPaper;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.util.ArrayList;
import java.util.List;

/**
 * I tried here to implement the TreasureHunt2 Algorithm,
 * which for angles beta &lt; 2 * PI has traveling time in O(n ^ (2 - epsilon)).
 *
 * This strategy might be useful if the Agent is not allowed to remember hints
 * and only use the current one, else it's a useless strategy.
 * Also, runtime and space complexity is exponential.
 *
 * However the Index Function from the paper is way too imprecise for practical use
 * (the Tiling gets way too small).
 * So I made small adjustments but it still is not good enough.
 *
 *
 * Also, for convenience the strategy runs on a thread (TODO: destroy thread when strategy aborted/finished)
 * to get the hints
 * (pausing main thread while running strategy and pausing strategy while waiting for main thread to get hint)
 *
 * @author Vincent Schönbach
 */
public class StrategyFromPaper2 implements Searcher<AngleHint> {
    private Coordinate location;
    private TreasureHunt2Thread thread;
    private Movement nextMoves;
    private AngleHint currentHint;
    private Object syncObj = new Object();
    private boolean flagMoveIsSet = false;
    private int IndexMax;
    private List<GeometryItem> itemsInPhase = new ArrayList<>();

    private static class TreasureHunt2Thread extends Thread{
        StrategyFromPaper2 strategy;
        TreasureHunt2Thread(StrategyFromPaper2 s){
            strategy = s;
        }
        public void run(){
            strategy.TreasureHunt2();
        }
    }

    /*Basically, function from paper.*/
    private void TreasureHunt2(){
        /*for (int i = 1; i <= 360; i++){
            System.out.println("Index( " + i + ") = "+  index(Angle.toRadians(i)));
        }*/


        int IndexNew = 1;
        int IndexOld;
        int i = 1;
        while(true){
            do{
                //System.out.println("i: " + i + " IndexNew = "+  IndexNew);

                //Clear items for next phase
                nextMoves.getToBeRemoved().addAll(itemsInPhase);
                itemsInPhase = new ArrayList<>();

                IndexOld = IndexNew;
                IndexNew = Mosaic(i, IndexOld);
            } while(IndexNew != IndexOld);
            i++;
        }
    }

    /*Basically, function from paper.*/
    private int Mosaic(int i , int k) {
        Coordinate O = location.copy();
        int length = (int)Math.pow(2, i);
        Tile S = new Tile(new Coordinate(O.getX()-length/2., O.getY()+length/2.), length, nextMoves);

        IndexMax = k;
        int maxSteps = calcMaxSteps(i, k);
        //System.out.println("maxSteps: " + maxSteps);

        for (int j = 1; j <= maxSteps; j++){
            S.createTiling((j-1)*k, nextMoves);
            iterateTiling(S, k); // inner for-loop from paper
        }

        if (IndexMax == k){
            S.createTiling(k*calcMaxSteps(i, k), nextMoves);
            iterateTiling3(S); // For-loop from paper
        }
        gotoPoint(O);
        return IndexMax;
    }

    private void iterateTiling(Tile t, int k) {
        if (t.subtiles == null)
        {
            if (t.getColor() == Tile.Color.white){
                gotoPoint(t.getCenter()); //Go to the center of t

                //Wait for Hint in main thread
                synchronized (syncObj){
                    syncObj.notify();
                    flagMoveIsSet = true;
                }
                synchronized (thread) {
                    try {
                        thread.wait();
                    } catch (InterruptedException e) {
                        //System.out.println("Error waiting for hint in Mosaic().");
                    }
                }
                GeometryAngle geometryAngle = currentHint.getGeometryAngle().copy();
                int k_ = StrategyFromPaper2.index(2*Math.PI - geometryAngle.extend()); // k'

                if (k_ > IndexMax) {
                    IndexMax = k_;
                }
                if (IndexMax == k) {
                    t.createTiling(k, nextMoves);
                    iterateTiling2(t);
                }
            }
        }
        else {
            for (int p = 0; p < 4; p++) {
                iterateTiling(t.subtiles[p], k);
            }
        }
    }

    private void iterateTiling2(Tile t) {
        if (t.subtiles == null)
        {
            GeometryAngle geometryAngle = new GeometryAngle(currentHint.getGeometryAngle().getLeft().copy(),
                    currentHint.getGeometryAngle().getCenter().copy(),
                    currentHint.getGeometryAngle().getRight().copy());
            if (t.isInsideOfAngle(geometryAngle)) {
                t.drawBlack(nextMoves); //Paint black all points of t'
            }
        }
        else {
            for (int j = 0; j < 4; j++) {
                iterateTiling2(t.subtiles[j]);
            }
        }
    }

    private void iterateTiling3(Tile t) {
        if (t.subtiles == null)
        {
            if (t.getColor() == Tile.Color.white){
                gotoPoint(t.getCenter());

                //Execute RectangleScan(t)
                Movement addMoves = StrategyFromPaper.rectangleScan(
                        JTSUtils.GEOMETRY_FACTORY.createPoint(t.getA()),
                        JTSUtils.GEOMETRY_FACTORY.createPoint(t.getB()),
                        JTSUtils.GEOMETRY_FACTORY.createPoint(t.getC()),
                        JTSUtils.GEOMETRY_FACTORY.createPoint(t.getD()));

                for (GeometryItem<Point> p : addMoves.getPoints()){
                    nextMoves.addWayPoint(p.getObject());
                }

                //Draw Movement of Rectangle Scan
                Coordinate[] linesToDraw = new Coordinate[addMoves.getPoints().size()+1];
                linesToDraw[0] = location.copy();
                for (int i1 = 0; i1 < addMoves.getPoints().size(); i1++)
                {
                    linesToDraw[i1+1] = addMoves.getPoints().get(i1).getObject().getCoordinate();
                }
                nextMoves.addAdditionalItem(new GeometryItem<>(new LineString(new CoordinateArraySequence(linesToDraw),
                        JTSUtils.GEOMETRY_FACTORY), GeometryType.RECTANGLE_SCAN_MOVEMENT));


                gotoPoint(t.getCenter());
            }
        }
        else {
            for (int j = 0; j < 4; j++) {
                iterateTiling3(t.subtiles[j]);
            }
        }
    }


    @Override
    public Movement move() {
        synchronized (syncObj) {
            if (!flagMoveIsSet) {
                try {
                    syncObj.wait();
                } catch (InterruptedException e) {
                    System.out.println("Error waiting in move().");
                }
            }
        }
        return nextMoves;
    }

    @Override
    public Movement move(AngleHint hint) {

        nextMoves = new Movement(JTSUtils.GEOMETRY_FACTORY.createPoint(location));
        currentHint = hint;

        //Wait for Movement in strategy thread
        synchronized (thread) {
            thread.notify();
        }
        synchronized (syncObj) {
            try {
                syncObj.wait();
            } catch (InterruptedException e) {
                System.out.println("Error waiting in move().");
            }
        }

        itemsInPhase.addAll(nextMoves.getAdditionalGeometryItems());

        return nextMoves;
    }

    @Override
    public void init(Point startPosition) {
        location = startPosition.getCoordinate();
        nextMoves = new Movement(startPosition);
        thread = new TreasureHunt2Thread(this);
        thread.start();
    }

    private void gotoPoint(Coordinate c){
        nextMoves.addWayPoint(JTSUtils.GEOMETRY_FACTORY.createPoint(c));
        Coordinate[] coordinates = {location, c};
        nextMoves.addAdditionalItem(new GeometryItem<>(new LineString(new CoordinateArraySequence(coordinates),
                JTSUtils.GEOMETRY_FACTORY), GeometryType.SEARCHER_MOVEMENT));
        location = c.copy();
    }


    private static int calcMaxSteps(int i, int k){
        /*Equivalent to formula from paper:
        Math.ceil(Math.log(Math.sqrt(Math.pow(2, i))) / (Math.log(Math.pow(4,k))))*/
        //return (int)Math.ceil(0.25*i/(double)k);

        // Custom modified max Steps Function which works maybe a tiny bit better:
        return (int)Math.ceil(i/(double)k);
    }

    /*The index function which is way too big, all values are >= 12
    * which would mean Tilings of size are greater than 4^12 !!!*/
    private static int index(double alpha){
        //return 4*((int)Math.max(3, Math.ceil(Math.log(2.*Math.PI/alpha)/Math.log(2.)) + 1));

        // Custom modified index Function which works a tiny bit better:
        return (int) (0.5*Math.ceil(Math.log(2.*Math.PI/alpha)/Math.log(2.)))+1;
    }

}
