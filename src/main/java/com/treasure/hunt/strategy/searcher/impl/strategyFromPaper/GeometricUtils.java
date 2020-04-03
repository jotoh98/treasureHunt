package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;

import static com.treasure.hunt.utils.JTSUtils.doubleEqual;

/**
 * @author Rank
 */
public class GeometricUtils {
    private GeometricUtils() {
    }

    static void assertRectangle(Coordinate[] rectangle) {
        if (rectangle.length != 4) {
            throw new IllegalArgumentException("The rectangle has " + rectangle.length + " points. It should have 4.");
        }
    }

    static Point centerOfRectangle(Point P1, Point P2, Point P3, Point P4) {
        LineString line13 = JTSUtils.createLineString(P1, P3);
        return line13.getCentroid();
    }

    public static Coordinate centerOfRectangle(Coordinate[] rect) {
        LineSegment lineAC = new LineSegment(rect[0], rect[2]);
        return lineAC.midPoint();
    }

    public static SearchPath moveToCenterOfRectangle(Point P1, Point P2, Point P3, Point P4, SearchPath move) {
        move.addPoint(centerOfRectangle(P1, P2, P3, P4));
        return move;
    }

    /**
     * Coordinate of rect are rounded in the precision model grid and reordered so tha rect[0] is the upper left point,
     * rect[1] is the upper right point, rect[2] is the bottom right point and rect[3] is the bottom left point.
     * If the rectangle is not parallel to the x and y axis, an error is thrown.
     *
     * @param rect
     * @return
     */
    static Coordinate[] arrangeRectangle(Coordinate[] rect) {
        assertRectangle(rect);

        /**
         newABCD is the new (rearranged) rectangle
         newA is the point left top, newB right top, newC right bottom and newD left bottom.
         */
        Coordinate newA = null, newB = null, newC = null, newD = null;
        /**
         * maxX represents the maximum value of an x-coordinate of one of the edges in the rectangle rect.
         * maxY, minX and minY are set equivalent.
         */
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            double x = rect[i].getX();
            double y = rect[i].getY();

            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);

            if (doubleEqual(minX, x) && doubleEqual(maxY, y)) {
                newA = rect[i];
            }
            if (doubleEqual(maxX, x) && doubleEqual(maxY, y)) {
                newB = rect[i];
            }
            if (doubleEqual(maxX, x) && doubleEqual(minY, y)) {
                newC = rect[i];
            }
            if (doubleEqual(minX, x) && doubleEqual(minY, y)) {
                newD = rect[i];
            }
        }
        if (newA == null || newB == null || newC == null || newD == null) {
            throw new IllegalArgumentException("rect is malformed. It equals " + rect[0] + rect[1] + rect[2] + rect[3]);
        }

        if (newA.equals2D(newB) || newA.equals2D(newC) || newA.equals2D(newD) || newB.equals2D(newC) || newB.equals2D(newD) || newC.equals2D(newD)) {
            throw new IllegalArgumentException("rect is malformed. It equals " + rect[0] + rect[1] + rect[2] + rect[3]);
        }

        if (!doubleEqual(newA.x, newD.x) || !doubleEqual(newA.y, newB.y) || !doubleEqual(newB.x, newC.x) || !doubleEqual(newC.y, newD.y)) {
            throw new IllegalArgumentException("rect is not parallel to x an y axis:" +
                    "\nrect[0] = " + rect[0] +
                    "\nrect[1] = " + rect[1] +
                    "\nrect[2] = " + rect[2] +
                    "\nrect[3] = " + rect[3] +
                    "\nnewA = " + newA +
                    "\nnewB = " + newB +
                    "\nnewC = " + newC +
                    "\nnewD = " + newD
            );
        }
        Coordinate[] rectRes = new Coordinate[]{newA, newB, newC, newD};
        return rectRes;
    }

    static Coordinate twoStepsOrthogonal(HalfPlaneHint hint, Point P) {
        return twoStepsOrthogonal(hint, P.getCoordinate());
    }

    static Coordinate twoStepsOrthogonal(HalfPlaneHint hint, Coordinate cur_pos) {
        Vector2D hintVector = new Vector2D(hint.getCenter(),
                hint.getRight());

        hintVector = hintVector.divide(hintVector.length() / 2);
        hintVector = hintVector.rotateByQuarterCircle(1);
        return new Coordinate(cur_pos.getX() + hintVector.getX(), cur_pos.getY() + hintVector.getY());
    }
}
