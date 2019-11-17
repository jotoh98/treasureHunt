package com.treasure.hunt.strategy.searcher.implementations;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.hint.AngleHint;
import com.treasure.hunt.strategy.hint.HalfplaneHint;
import com.treasure.hunt.strategy.hint.HalfplaneHint.Direction;
import com.treasure.hunt.strategy.searcher.Moves;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.LineSegment;
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
    public Moves move() {
        return incrementPhase();
    }

    @Override
    public Moves move(AngleHint hint) {
        double width = B.getX() - A.getX();
        double height = A.getY() - D.getY();
        if (width * height <= 4) {
            return incrementPhase();
        }
        //now analyse the hint:
        HalfplaneHint piHint;
        piHint = angular2correctHalfPlaneHint(hint);

        LineSegment AB = new LineSegment(A.getCoordinate(), B.getCoordinate());
        LineSegment BC = new LineSegment(B.getCoordinate(), C.getCoordinate());
        LineSegment CD = new LineSegment(C.getCoordinate(), D.getCoordinate());
        LineSegment AD = new LineSegment(A.getCoordinate(), D.getCoordinate());

        LineSegment piHintLine = new LineSegment(piHint.getHalfplanePointOne().getCoordinate(),
                piHint.getHalfplanePointTwo().getCoordinate());

        Point intersection_AD_hint = JTSUtils.lineLinesegmentIntersection(piHintLine, AD);
        Point intersection_BC_hint = JTSUtils.lineLinesegmentIntersection(piHintLine, BC);

        Point intersection_AB_hint = JTSUtils.lineLinesegmentIntersection(piHintLine, AB);
        Point intersection_CD_hint = JTSUtils.lineLinesegmentIntersection(piHintLine, CD);

        Point []horizontalSplit = splitsRectangleHorizontally(A,B,C,D, piHint, intersection_AD_hint,
                intersection_BC_hint);
        if(horizontalSplit != null)
        {
            A = horizontalSplit[0];
            B = horizontalSplit[1];
            C = horizontalSplit[2];
            D = horizontalSplit[3];
            return movesToCenterOfRectangle(A,B,C,D);
        }
        Point []verticalSplit = splitsRectangleVertically(A,B,C,D, piHint, intersection_AB_hint,
                intersection_CD_hint);
        if(verticalSplit != null)
        {
            A = verticalSplit[0];
            B = verticalSplit[1];
            C = verticalSplit[2];
            D = verticalSplit[3];
            return movesToCenterOfRectangle(A,B,C,D);
        }
        return badHintSubroutine(piHint);
    }

    @Override
    public Point getLocation() {
        return location;
    }

    private Point[] splitsRectangleHorizontally(Point A, Point B, Point C, Point D, HalfplaneHint piHint,
                                                Point intersection_AD_hint, Point intersection_BC_hint) {
        if (intersection_AD_hint == null || intersection_AD_hint == null) {
            return null;
        }

        if (piHint.getDirection() == Direction.up) {
            if ((intersection_AD_hint.getY() - D.getY()) >= 1) {
                Point newD = intersection_AD_hint;
                Point newC = intersection_BC_hint;
                return new Point[]{A, B, newC, newD};
            }
        }

        if (piHint.getDirection() == Direction.down) {
            if ((A.getY() - intersection_AD_hint.getY()) >= 1) {
                Point newA = intersection_AD_hint;
                Point newB = intersection_BC_hint;
                return new Point[]{newA, newB, C, D};
            }
        }

        Point lowerHintPoint;
        Point upperHintPoint;

        if (piHint.getHalfplanePointOne().getY() < piHint.getHalfplanePointTwo().getY()) {
            lowerHintPoint = piHint.getHalfplanePointOne();
            upperHintPoint = piHint.getHalfplanePointTwo();
        } else {
            lowerHintPoint = piHint.getHalfplanePointTwo();
            upperHintPoint = piHint.getHalfplanePointOne();
        }

        if ((piHint.getDirection() == Direction.left && lowerHintPoint.getX() < upperHintPoint.getX()) ||
                (piHint.getDirection() == Direction.right && lowerHintPoint.getX() > upperHintPoint.getX()))
        //the hint points upwards
        {
            if (intersection_AD_hint.distance(D) >= 1 && intersection_BC_hint.distance(C) >= 1) {
                if (intersection_AD_hint.distance(D) >= intersection_BC_hint.distance(C)) {
                    Point newD = JTSUtils.createPoint(D.getX(), intersection_BC_hint.getY());
                    Point newC = intersection_BC_hint;
                    return new Point[]{A, B, newC, newD};
                } else {
                    Point newC = JTSUtils.createPoint(C.getX(), intersection_AD_hint.getY());
                    Point newD = intersection_AD_hint;
                    return new Point[]{A, B, newC, newD};
                }
            }
        }
        if ((piHint.getDirection() == Direction.left && lowerHintPoint.getX() > upperHintPoint.getX()) ||
                (piHint.getDirection() == Direction.right && lowerHintPoint.getX() < upperHintPoint.getX()))
        //the hint points downwards
        {
            if (intersection_AD_hint.distance(A) >= intersection_BC_hint.distance(B)) {
                Point newA = JTSUtils.createPoint(A.getX(), intersection_BC_hint.getY());
                Point newB = intersection_BC_hint;
                return new Point[]{newA, newB, C, D};
            } else {
                Point newB = JTSUtils.createPoint(B.getX(), intersection_AD_hint.getY());
                Point newA = intersection_AD_hint;
                return new Point[]{newA, newB, C, D};
            }
        }
        return null;
    }

    private Point[] splitsRectangleVertically(Point A, Point B, Point C, Point D, HalfplaneHint piHint,
                                              Point intersection_AB_hint, Point intersection_CD_hint) {
        if (piHint.getDirection() == Direction.left) {
            if (intersection_AB_hint != null) {
                if (intersection_AB_hint.distance(B) >= 1 && intersection_CD_hint.distance(C) >= 1) {
                    // checks if y is bigger or equal to 0
                    // determine which intersection-point has to be used to calculate the rectangle-points:
                    if (intersection_AB_hint.distance(B) >= intersection_CD_hint.distance(C)) {
                        Point newB = JTSUtils.createPoint(intersection_CD_hint.getX(), B.getY());
                        Point newC = intersection_CD_hint;
                        return new Point[]{A, newB, newC, D};
                    } else {
                        // equivalent
                        Point newC = JTSUtils.createPoint(intersection_AB_hint.getX(), C.getY());
                        Point newB = intersection_AB_hint;
                        return new Point[]{A, newB, newC, D};
                    }
                }
            }
        }

        if (piHint.getDirection() == Direction.right) {
            if (intersection_AB_hint != null) {
                if (intersection_AB_hint.distance(B) >= 1 && intersection_CD_hint.distance(C) >= 1) {
                    // checks if y is bigger or equal to 0
                    // determine which intersection-point has to be used to calculate the rectangle-points:
                    if (intersection_AB_hint.distance(A) >= intersection_CD_hint.distance(D)) {
                        Point newA = JTSUtils.createPoint(intersection_CD_hint.getX(), A.getY());
                        Point newD = intersection_CD_hint;
                        return new Point[]{newA, B, C, newD};
                    } else {
                        // equivalent
                        Point newD = JTSUtils.createPoint(intersection_AB_hint.getX(), D.getY());
                        Point newA = intersection_AB_hint;
                        return new Point[]{newA, B, C, newD};
                    }
                }
            }
        }
        return null;
    }

    private Moves badHintSubroutine(HalfplaneHint hint) {
        return null;
    }

    private Moves twoHintsSubroutine(HalfplaneHint firstHint, HalfplaneHint secondHint) {
        return null;
    }

    private Moves movesToCenterOfRectangle(Point P1, Point P2, Point P3, Point P4) {
        LineString line13 = JTSUtils.createLineString(P1, P3);
        Moves ret = new Moves();
        ret.addWayPoint(line13.getCentroid());
        return ret;
    }

    private Moves incrementPhase() {
        phase++;
        Point oldA = A;
        Point oldB = B;
        Point oldC = C;
        Point oldD = D;
        setRectToPhase();
        return rectangleScan(oldA, oldB, oldC, oldD); //TODO maybe go to the middle of the new rectangle
    }

    private void setRectToPhase() {
        double halfDiff = Math.pow(2, phase - 1);
        double startX = start.getX();
        double startY = start.getY();
        A = JTSUtils.createPoint(startX - halfDiff, startY + halfDiff);
        B = JTSUtils.createPoint(startX + halfDiff, startY + halfDiff);
        C = JTSUtils.createPoint(startX - halfDiff, startY + halfDiff);
        D = JTSUtils.createPoint(startX - halfDiff, startY - halfDiff);
    }


    private Moves rectangleScan(Point A, Point B, Point C, Point D) {
        return null;
    }
}

