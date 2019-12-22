package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.Direction;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import static com.treasure.hunt.strategy.hint.impl.HalfPlaneHint.angular2correctHalfPlaneHint;

/**
 * This implements the strategy from the paper:
 * {@literal Treasure Hunt in the Plane with Angular Hints}
 *
 * @author Rank
 */
public class StrategyFromPaper implements Searcher<HalfPlaneHint> {
    int phase; //equals j in the paper. In phase i, the algorithm checks a rectangle with a side length of 2^i
    Point start,
            location,
            A, B, C, D;

    /**
     * {@inheritDoc}
     */
    public void init(Point startPosition, int width, int height) {
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
        if (width * height <= 4) {
            return incrementPhase();
        }
        //now analyse the hint:
        HalfPlaneHint piHint;
        piHint = angular2correctHalfPlaneHint(hint); // TODO is this necessary ?

        LineSegment AB = new LineSegment(A.getCoordinate(), B.getCoordinate());
        LineSegment BC = new LineSegment(B.getCoordinate(), C.getCoordinate());
        LineSegment CD = new LineSegment(C.getCoordinate(), D.getCoordinate());
        LineSegment AD = new LineSegment(A.getCoordinate(), D.getCoordinate());

        LineSegment piHintLine = new LineSegment(piHint.getGeometryAngle().getCenter(),
                piHint.getHalfPlanePoint());

        Point intersection_AD_hint = JTSUtils.lineLineSegmentIntersection(piHintLine, AD);
        Point intersection_BC_hint = JTSUtils.lineLineSegmentIntersection(piHintLine, BC);

        Point intersection_AB_hint = JTSUtils.lineLineSegmentIntersection(piHintLine, AB);
        Point intersection_CD_hint = JTSUtils.lineLineSegmentIntersection(piHintLine, CD);

        Point[] horizontalSplit = splitRectangleHorizontally(A, B, C, D, piHint, intersection_AD_hint,
                intersection_BC_hint);
        if (horizontalSplit != null) {
            A = horizontalSplit[0];
            B = horizontalSplit[1];
            C = horizontalSplit[2];
            D = horizontalSplit[3];
            return movesToCenterOfRectangle(A, B, C, D);
        }
        Point[] verticalSplit = splitRectangleVertically(A, B, C, D, piHint, intersection_AB_hint,
                intersection_CD_hint);
        if (verticalSplit != null) {
            A = verticalSplit[0];
            B = verticalSplit[1];
            C = verticalSplit[2];
            D = verticalSplit[3];
            return movesToCenterOfRectangle(A, B, C, D);
        }
        return badHintSubroutine(piHint);
    }

    private Point[] splitRectangleHorizontally(Point A, Point B, Point C, Point D, HalfPlaneHint piHint,
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

        Coordinate lowerHintPoint;
        Coordinate upperHintPoint;

        if (piHint.getGeometryAngle().getCenter().getY() < piHint.getHalfPlanePoint().getY()) {
            lowerHintPoint = piHint.getGeometryAngle().getCenter();
            upperHintPoint = piHint.getHalfPlanePoint();
        } else {
            lowerHintPoint = piHint.getHalfPlanePoint();
            upperHintPoint = piHint.getGeometryAngle().getCenter();
        }

        if (piHint.pointsUpwards()) {
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
        if (piHint.pointsDownwards()) {
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

    private Point[] splitRectangleVertically(Point A, Point B, Point C, Point D, HalfPlaneHint piHint,
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

    private Movement badHintSubroutine(HalfPlaneHint hint) {
        return null;
    }

    private Movement twoHintsSubroutine(HalfPlaneHint firstHint, HalfPlaneHint secondHint) {
        return null;
    }

    private Point twoStepsOrthogonal(HalfPlaneHint piHint) {
        if (piHint.getDirection() == Direction.up) {
            return JTSUtils.createPoint(location.getX(), location.getY() + 2);
        }
        if (piHint.getDirection() == Direction.down) {
            return JTSUtils.createPoint(location.getX(), location.getY() - 2);
        }

        return null;
    }

    private Movement movesToCenterOfRectangle(Point P1, Point P2, Point P3, Point P4) {
        LineString line13 = JTSUtils.createLineString(P1, P3);
        Movement ret = new Movement(line13.getCentroid());
        return ret;
    }

    private Movement incrementPhase() {
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

    private Movement rectangleScan(Point A, Point B, Point C, Point D) {
        Movement movements = new Movement();

        int k = (int) A.distance(B);
        Point[] a = new Point[k];
        Point[] b = new Point[k];

        { //create a_i on line segment AB
            double xDist = B.getX() - A.getX();
            double yDist = B.getY() - A.getY();
            for (int i = 0; i <= k; i++) {
                a[i] = JTSUtils.createPoint(A.getX() + xDist * ((double) i / k), B.getX() + yDist * ((double) i / k));
            }
        }
        { //create b_i on line segment DC
            double xDist = D.getX() - C.getX();
            double yDist = D.getY() - C.getY();
            for (int i = 0; i <= k; i++) {
                b[i] = JTSUtils.createPoint(D.getX() + xDist * ((double) i / k), C.getX() + yDist * ((double) i / k));
            }
        }

        if (k % 2 == 1) //code like in paper
        {
            for (int i = 0; i <= k - 1; k += 2) {
                movements.addWayPoint(a[i]);
                movements.addWayPoint(b[i]);
                movements.addWayPoint(b[i + 1]);
                movements.addWayPoint(a[i + 1]);
            }
        } else {
            for (int i = 0; i <= k - 2; k += 2) {
                movements.addWayPoint(a[i]);
                movements.addWayPoint(b[i]);
                movements.addWayPoint(b[i + 1]);
                movements.addWayPoint(a[i + 1]);
            }
            movements.addWayPoint(a[k]);
            movements.addWayPoint(b[k]);
            //moves.addWayPoint(a); // go to a
        }
        return movements;
    }
}