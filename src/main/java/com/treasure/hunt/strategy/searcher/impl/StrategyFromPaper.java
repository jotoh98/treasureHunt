package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.game.GameHistory;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.math.Vector2D;

import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction.*;

public class StrategyFromPaper implements Searcher<HalfPlaneHint> {
    int phase; //equals j in the paper. In phase i, the algorithm checks a rectangle with a side length of 2^i
    Point start,
            location,
            A, B, C, D;

    HalfPlaneHint lastBadHint; //only used when last hint was bad
    boolean lastHintWasBad = false;

    @Override
    public void init(Point startPosition) {
        start = startPosition;
        location = (Point) startPosition.copy();
        phase = 1;
        setRectToPhase();
    }

    @Override
    public Movement move() {
        return incrementPhase();
    }

    @Override
    public Movement move(HalfPlaneHint hint) {
        double width = B.getX() - A.getX();
        double height = A.getY() - D.getY();
        if (width < 4 || height < 4) {
            return incrementPhase();
        }
        //now analyse the hint:
        if (lastHintWasBad)
            return twoHintsSubroutine(hint);

        LineSegment AB = new LineSegment(A.getCoordinate(), B.getCoordinate());
        LineSegment BC = new LineSegment(B.getCoordinate(), C.getCoordinate());
        LineSegment CD = new LineSegment(C.getCoordinate(), D.getCoordinate());
        LineSegment AD = new LineSegment(A.getCoordinate(), D.getCoordinate());

        LineSegment hintLine = new LineSegment(hint.getCenter().getCoordinate(),
                hint.getHalfPlanePoint().getCoordinate());

        Point intersection_AD_hint = JTSUtils.lineLineSegmentIntersection(hintLine, AD);
        Point intersection_BC_hint = JTSUtils.lineLineSegmentIntersection(hintLine, BC);

        Point intersection_AB_hint = JTSUtils.lineLineSegmentIntersection(hintLine, AB);
        Point intersection_CD_hint = JTSUtils.lineLineSegmentIntersection(hintLine, CD);

        Point[] horizontalSplit = splitRectangleHorizontally(A, B, C, D, hint, intersection_AD_hint,
                intersection_BC_hint);
        if (horizontalSplit != null) {
            A = horizontalSplit[0];
            B = horizontalSplit[1];
            C = horizontalSplit[2];
            D = horizontalSplit[3];
            return movesToCenterOfRectangle(A, B, C, D);
        }
        Point[] verticalSplit = splitRectangleVertically(A, B, C, D, hint, intersection_AB_hint,
                intersection_CD_hint);
        if (verticalSplit != null) {
            A = verticalSplit[0];
            B = verticalSplit[1];
            C = verticalSplit[2];
            D = verticalSplit[3];
            return movesToCenterOfRectangle(A, B, C, D);
        }
        return badHintSubroutine(hint);
    }

    private Point[] splitRectangleHorizontally(Point A, Point B, Point C, Point D, HalfPlaneHint hint,
                                               Point intersection_AD_hint, Point intersection_BC_hint) {
        if (intersection_AD_hint == null || intersection_AD_hint == null) {
            return null;
        }

        if (hint.getDirection() == up) {
            if ((intersection_AD_hint.getY() - D.getY()) >= 1) {
                Point newD = intersection_AD_hint;
                Point newC = intersection_BC_hint;
                return new Point[]{A, B, newC, newD};
            }
        }

        if (hint.getDirection() == down) {
            if ((A.getY() - intersection_AD_hint.getY()) >= 1) {
                Point newA = intersection_AD_hint;
                Point newB = intersection_BC_hint;
                return new Point[]{newA, newB, C, D};
            }
        }

        if (hint.pointsUpwards()) {
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
        if (hint.pointsDownwards()) {
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

    private Point[] splitRectangleVertically(Point A, Point B, Point C, Point D, HalfPlaneHint hint,
                                             Point intersection_AB_hint, Point intersection_CD_hint) {
        // checks if the hint is good (i.e. if the hint divides one side of the rectangles in into two parts such that
        // the smaller one is bigger or equal to 1)
        if (intersection_AB_hint == null || intersection_CD_hint == null
                || (intersection_AB_hint.distance(A) < 1 || intersection_AB_hint.distance(B) < 1
                || intersection_CD_hint.distance(C) < 1 || intersection_CD_hint.distance(D) < 1)) {
            return null;
        }

        if (hint.getDirection() == left) {
            // determine which intersection-point has to be used to calculate the rectangle-points:
            if (intersection_AB_hint.distance(B) >= intersection_CD_hint.distance(C)) {
                Point newB = JTSUtils.createPoint(intersection_CD_hint.getX(), B.getY());
                Point newC = intersection_CD_hint;
                return new Point[]{A, newB, newC, D};
            } else {
                Point newC = JTSUtils.createPoint(intersection_AB_hint.getX(), C.getY());
                Point newB = intersection_AB_hint;
                return new Point[]{A, newB, newC, D};
            }
        }

        if (hint.getDirection() == right) {
            // determine which intersection-point has to be used to calculate the rectangle-points:
            if (intersection_AB_hint.distance(A) >= intersection_CD_hint.distance(D)) {
                Point newA = JTSUtils.createPoint(intersection_CD_hint.getX(), A.getY());
                Point newD = intersection_CD_hint;
                return new Point[]{newA, B, C, newD};
            } else {
                Point newD = JTSUtils.createPoint(intersection_AB_hint.getX(), D.getY());
                Point newA = intersection_AB_hint;
                return new Point[]{newA, B, C, newD};
            }
        }
        return null;
    }

    private Movement badHintSubroutine(HalfPlaneHint hint) {
        //return movesToCenterOfRectangle(A, B, C, D); //testing

        Point direction = twoStepsOrthogonal(hint, location);
        Movement ret = new Movement();
        ret.addWayPoint(direction);
        lastHintWasBad = true;
        lastBadHint = hint;
        return ret;
    }

    // location has to be set accordingly (so that the player is on the hint line)
    private Point twoStepsOrthogonal(HalfPlaneHint hint, Point cur_pos) {
        Vector2D hintVector = new Vector2D(hint.getLowerHintPoint().getCoordinate(),
                hint.getUpperHintPoint().getCoordinate());

        hintVector = hintVector.divide(hintVector.length() / 2);

        switch (hint.getDirection()) {
            case up:
                return JTSUtils.createPoint(cur_pos.getX(), cur_pos.getY() + 2);
            case down:
                return JTSUtils.createPoint(cur_pos.getX(), cur_pos.getY() - 2);
            case left:
                hintVector = hintVector.rotateByQuarterCircle(1);
            case right:
                hintVector = hintVector.rotateByQuarterCircle(3);
        }
        return JTSUtils.createPoint(cur_pos.getX() + hintVector.getX(), cur_pos.getY() + hintVector.getY());
    }

    private Point centerOfRectangle(Point P1, Point P2, Point P3, Point P4) {
        LineString line13 = JTSUtils.createLineString(P1, P3);
        return line13.getCentroid();
    }

    private Movement movesToCenterOfRectangle(Point P1, Point P2, Point P3, Point P4) {
        Movement ret = new Movement();
        ret.addWayPoint(centerOfRectangle(P1, P2, P3, P4));
        return ret;
    }

    private Movement incrementPhase() {
        phase++;
        Point oldA = A;
        Point oldB = B;
        Point oldC = C;
        Point oldD = D;
        setRectToPhase();
        Movement ret = rectangleScan(oldA, oldB, oldC, oldD);
        ret.addWayPoint(centerOfRectangle(A, B, C, D));
        return ret;
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

    private Movement rectangleScan(Point A, Point B, Point C, Point D) {
        Movement moves = new Movement();

        int k = (int) A.distance(B);
        Point[] a = new Point[k];
        Point[] b = new Point[k];

        { //create a_i on line segment AB
            double xDist = B.getX() - A.getX();
            double yDist = B.getY() - A.getY();
            for (int i = 0; i <= k; i++) {
                a[i] = JTSUtils.createPoint(A.getX() + xDist * ((double) i / k), B.getX() + yDist *
                        ((double) i / k));
            }
        }
        { //create b_i on line segment DC
            double xDist = D.getX() - C.getX();
            double yDist = D.getY() - C.getY();
            for (int i = 0; i <= k; i++) {
                b[i] = JTSUtils.createPoint(D.getX() + xDist * ((double) i / k), C.getX() + yDist *
                        ((double) i / k));
            }
        }

        if (k % 2 == 1) //code like in paper
        {
            for (int i = 0; i <= k - 1; k += 2) {
                moves.addWayPoint(a[i]);
                moves.addWayPoint(b[i]);
                moves.addWayPoint(b[i + 1]);
                moves.addWayPoint(a[i + 1]);
            }
        } else {
            for (int i = 0; i <= k - 2; k += 2) {
                moves.addWayPoint(a[i]);
                moves.addWayPoint(b[i]);
                moves.addWayPoint(b[i + 1]);
                moves.addWayPoint(a[i + 1]);
            }
            moves.addWayPoint(a[k]);
            moves.addWayPoint(b[k]);
            //moves.addWayPoint(a); // go to a
        }
        return moves;
    }

    private Movement twoHintsSubroutine(HalfPlaneHint curHint) {
        //TODO

        // plan für diese funktion:
        // erstmal die methoden für phi, reversePhi, rho und getBasicTransformation schreiben
        // dann die ganzen punkte berechnen die in dem Paper auch gebraucht werden
        // dann die Fälle des Papers durchgehen und dementsprechend returnen

        return null;
    }


    @Value
    private class RectangleHintPair {
        Point A;
        Point B;
        Point C;
        Point D;
        HalfPlaneHint hint;
    }

    /**
     * Returnes the rectangle R of rp and the hint of rp which are mirrored in H, a vertical line
     * through the center of R.
     *
     * @param rp
     * @return
     */
    private RectangleHintPair rho(RectangleHintPair rp) {
        Point A = rp.getA();
        Point B = rp.getB();
        Point C = rp.getC();
        Point D = rp.getD();

        LineString AD = JTSUtils.GEOMETRY_FACTORY.createLineString(new Coordinate[]{
                A.getCoordinate(), D.getCoordinate()});
        LineString BC = JTSUtils.GEOMETRY_FACTORY.createLineString(new Coordinate[]{
                B.getCoordinate(), C.getCoordinate()});
        Point middleOfAD = AD.getCentroid();
        Point middleOfBC = BC.getCentroid();
        AffineTransformation reflectionH = AffineTransformation.reflectionInstance(
                middleOfAD.getX(), middleOfAD.getY(), middleOfBC.getX(), middleOfBC.getY());


        //Point r = centerOfRectangle(rp.getA(),rp.getB(),rp.getC(),rp.getD());

        return null;
    }

    private Point[] phi(int k, Point A, Point B, Point C, Point D) {

        return null;
    }

    private Point[] reversePhi(int k, Point A, Point B, Point C, Point D) {

        return null;
    }

    private int getBasicTransformation(RectangleHintPair rp) {

        return 0;
    }

}

