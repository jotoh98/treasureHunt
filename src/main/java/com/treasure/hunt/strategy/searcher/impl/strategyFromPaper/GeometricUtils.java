package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;

import static com.treasure.hunt.utils.JTSUtils.doubleEqual;

/**
 * @author bsen
 */
public class GeometricUtils {

    static void assertRectangle(Coordinate[] rectangle) {
        if (rectangle.length != 4)
            throw new IllegalArgumentException("The rectangle has " + rectangle.length + " points. It should have 4.");
    }

    static Point centerOfRectangle(Point P1, Point P2, Point P3, Point P4) {
        LineString line13 = JTSUtils.createLineString(P1, P3);
        return line13.getCentroid();
    }

    static Coordinate centerOfRectangle(Coordinate[] rect) {
        LineSegment lineAC = new LineSegment(rect[0], rect[2]);
        return lineAC.midPoint();
    }

    static Movement moveToCenterOfRectangle(Point P1, Point P2, Point P3, Point P4, Movement move) {
        move.addWayPoint(centerOfRectangle(P1, P2, P3, P4));
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
        double max_x = -Double.MAX_VALUE;
        double max_y = -Double.MAX_VALUE;
        double min_x = Double.MAX_VALUE;
        double min_y = Double.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            double x = rect[i].getX();
            double y = rect[i].getY();

            max_x = Math.max(max_x, x);
            max_y = Math.max(max_y, y);
            min_x = Math.min(min_x, x);
            min_y = Math.min(min_y, y);

            if (doubleEqual(min_x, x) && doubleEqual(max_y, y))
                newA = rect[i];
            if (doubleEqual(max_x, x) && doubleEqual(max_y, y))
                newB = rect[i];
            if (doubleEqual(max_x, x) && doubleEqual(min_y, y))
                newC = rect[i];
            if (doubleEqual(min_x, x) && doubleEqual(min_y, y))
                newD = rect[i];
        }
        if (newA == null || newB == null || newC == null || newD == null)
            throw new IllegalArgumentException("rect is malformed. It equals " + rect[0] + rect[1] + rect[2] + rect[3]);

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
