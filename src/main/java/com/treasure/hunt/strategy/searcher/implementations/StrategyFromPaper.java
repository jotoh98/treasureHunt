package com.treasure.hunt.strategy.searcher.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.Product;
import com.treasure.hunt.strategy.hint.AngleHint;
import com.treasure.hunt.strategy.hint.HalfplaneHint;
import com.treasure.hunt.strategy.hint.HalfplaneHint.Direction;
import com.treasure.hunt.strategy.searcher.Moves;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.strategy.searcher.movesImplementations.Steps;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.LineString;
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

        Point []intersection_AD_hint = JTSUtils.lineLinesegmentIntersection(piHint.getHalfplanePointOne(), piHint.getHalfplanePointTwo(), A, D);
        Point []intersection_BC_hint = JTSUtils.lineLinesegmentIntersection(piHint.getHalfplanePointOne(), piHint.getHalfplanePointTwo(), B, C);
        if(intersection_AD_hint.length>1)//TODO maybe use another exception
            throw new IllegalArgumentException("The hint intersects in more than one point with the Line AD");
        if(intersection_BC_hint.length>1)
            throw new IllegalArgumentException("The hint intersects in more than one point with the Line BC");

        if(piHint.getDirection()==Direction.up){
            if(intersection_AD_hint.length == 0){
                throw new IllegalArgumentException("The line doesn't intersect with AD and the direction is up," +
                        " that is not possible.");
            }
            if((intersection_AD_hint[0].getY()-D.getY())>=1){
                D = intersection_AD_hint[0];
                C = intersection_BC_hint[0];
                return stepsToCenterOfRectangle(A,B,C,D);
            }
        }

        if(piHint.getDirection()==Direction.down){
            if(intersection_AD_hint.length == 0){
                   throw new IllegalArgumentException("The line doesn't intersect with AD and the direction is down," +
                        " that is not possible.");
            }
            if((A.getY()-intersection_AD_hint[0].getY())>=1){
                A = intersection_AD_hint[0];
                B = intersection_BC_hint[0];
                return stepsToCenterOfRectangle(A,B,C,D);
            }
        }

        Point []intersection_AB_hint = JTSUtils.lineLinesegmentIntersection(piHint.getHalfplanePointOne(), piHint.getHalfplanePointTwo(), A, B);
        Point []intersection_CD_hint = JTSUtils.lineLinesegmentIntersection(piHint.getHalfplanePointOne(), piHint.getHalfplanePointTwo(), C, D);

        Point lowerHintPoint;
        Point upperHintPoint;

        if(piHint.getHalfplanePointOne().getY()<piHint.getHalfplanePointTwo().getY()){
            lowerHintPoint = piHint.getHalfplanePointOne();
            upperHintPoint = piHint.getHalfplanePointTwo();
        }
        else{
            lowerHintPoint = piHint.getHalfplanePointTwo();
            upperHintPoint = piHint.getHalfplanePointOne();
        }

        if(piHint.getDirection()==Direction.left){

        }



        return null;
    }

    @Override
    public Point getLocation() {
        return location;
    }

    private Steps badHintSubroutine(HalfplaneHint hint){
        return null;
    }

    private Steps twoHintsSubroutine(HalfplaneHint firstHint, HalfplaneHint secondHint){
        return null;
    }

    private Steps stepsToCenterOfRectangle(Point P1, Point P2, Point P3, Point P4){
        LineString line13 = JTSUtils.createLineString(P1, P3);
        Steps ret = new Steps();
        ret.addStep(line13.getCentroid());
        return ret;
    }

    private Moves incrementPhase(){
        phase++;
        Point oldA = A;
        Point oldB = B;
        Point oldC = C;
        Point oldD = D;
        setRectToPhase();
        return RectangeScan(oldA, oldB, oldC, oldD); //TODO maybe go to the middle of the new rectangle
    }

    private void setRectToPhase()
    {
        double halfDiff = Math.pow(2, phase-1);
        double startX = start.getX();
        double startY = start.getY();
        A = JTSUtils.createPoint(startX-halfDiff, startY+halfDiff);
        B = JTSUtils.createPoint(startX+halfDiff, startY+halfDiff);
        C = JTSUtils.createPoint(startX-halfDiff, startY+halfDiff);
        D = JTSUtils.createPoint(startX-halfDiff, startY-halfDiff);
    }


    private Moves RectangeScan(Point A, Point B, Point C, Point D){
        return null;
    }
}

