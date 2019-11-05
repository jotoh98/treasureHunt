package com.treasure.hunt.strategy.searcher.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.hint.AngleHint;
import com.treasure.hunt.strategy.hint.HalfplaneHint;
import com.treasure.hunt.strategy.hint.HalfplaneHint.Direction;
import com.treasure.hunt.strategy.searcher.Moves;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import static com.treasure.hunt.strategy.hint.HalfplaneHint.angular2correctHalfPlaneHint;

public class StrategyFromPaper implements Searcher<AngleHint> {
    int phase; //equals j in the paper. In phase i, the algorithm checks a rectangle with a side length of 2^i
    Point start;
    Point location;
    Point A;
    Point B;
    Point C;
    Point D;

    @Override
    public void init(Point startPosition, GameHistory gameHistory) {
        start = startPosition;
        location = (Point) startPosition.copy();
        phase = 1;
        setRectToPhase();
    }

    @Override
    public void commitProduct(Product product) {

    }

    @Override
    public Moves move() {
        return incrementPhase();
    }

    @Override
    public Moves move(AngleHint hint) {
        double width = B.getX()-A.getX();
        double height = A.getY()-D.getY();
        if(width*height <=4) {
            return incrementPhase();
        }
        //now analyse the hint:
        HalfplaneHint piHint;
        piHint = angular2correctHalfPlaneHint(hint);

        return null;
    }

    @Override
    public Point getLocation() {
        return location;
    }

    private Moves incrementPhase(){
        phase++;
        Point oldA = A;
        Point oldB = B;
        Point oldC = C;
        Point oldD = D;
        setRectToPhase();
        return RectangeScan(oldA, oldB, oldC, oldD);
    }

    private void setRectToPhase()
    {
        double halfDiff = Math.pow(2, phase-1);
        double startX = start.getX();
        double startY = start.getY();
        A = JTSUtils.givePoint(startX-halfDiff, startY+halfDiff);
        B = JTSUtils.givePoint(startX+halfDiff, startY+halfDiff);
        C = JTSUtils.givePoint(startX-halfDiff, startY+halfDiff);
        D = JTSUtils.givePoint(startX-halfDiff, startY-halfDiff);
    }


    private Moves RectangeScan(Point A, Point B, Point C, Point D){
        return null;
    }
}

