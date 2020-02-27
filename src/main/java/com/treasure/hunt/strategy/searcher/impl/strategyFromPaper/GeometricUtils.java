package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.math.Vector2D;

import static com.treasure.hunt.utils.JTSUtils.doubleEqual;

/**
 * @author bsen
 */
class GeometricUtils {
    private GeometricUtils() {
    }

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
     * Coordinate of rectangle are rounded in the precision model grid and reordered so tha rectangle[0] is the upper left point,
     * rectangle[1] is the upper right point, rectangle[2] is the bottom right point and rectangle[3] is the bottom left point.
     * If the rectangle is not parallel to the x and y axis, an error is thrown.
     *
     * @param rectangle
     * @return
     */
    static Coordinate[] arrangeRectangle(Coordinate[] rectangle, AffineTransformation fromAxisParallel,
                                         AffineTransformation toAxisParallel) {
        assertRectangle(rectangle);

        Coordinate[] axisParallelRectangle = new Coordinate[4];
        for (int i = 0; i < 4; i++) {
            axisParallelRectangle[i] = new Coordinate();
            toAxisParallel.transform(rectangle[i], axisParallelRectangle[i]);
        }

        /**
         newABCD is the new (rearranged) rectangle in axis parallel form (must be transformed to real form by
         fromAxisParallel-transformation (which is the identity in normal cases but when the strategyFromPaper is
         used by MinimumRectangleStrategy it may not be the identity))

         newA is the point left top, newB right top, newC right bottom and newD left bottom.
         */
        Coordinate newA = null, newB = null, newC = null, newD = null;
        /**
         * maxX represents the maximum value of an x-coordinate of one of the edges in the rectangle rectangle.
         * maxY, minX and minY are set equivalent.
         */
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            double x = axisParallelRectangle[i].getX();
            double y = axisParallelRectangle[i].getY();

            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);

            if (doubleEqual(minX, x) && doubleEqual(maxY, y))
                newA = axisParallelRectangle[i];
            if (doubleEqual(maxX, x) && doubleEqual(maxY, y))
                newB = axisParallelRectangle[i];
            if (doubleEqual(maxX, x) && doubleEqual(minY, y))
                newC = axisParallelRectangle[i];
            if (doubleEqual(minX, x) && doubleEqual(minY, y))
                newD = axisParallelRectangle[i];
        }
        if (toAxisParallel.equals(new AffineTransformation())) { // tests whether fromAxisParallel is the identity
            if ((newA == null || newB == null || newC == null || newD == null) ||
                    (newA.equals2D(newB) || newA.equals2D(newC) || newA.equals2D(newD) ||
                            newB.equals2D(newC) || newB.equals2D(newD) || newC.equals2D(newD))
            ) {
                throw new IllegalArgumentException("rectangle is malformed. It equals " + axisParallelRectangle[0] +
                        axisParallelRectangle[1] + axisParallelRectangle[2] + axisParallelRectangle[3]);
            }
            if (!doubleEqual(newA.x, newD.x) || !doubleEqual(newA.y, newB.y) || !doubleEqual(newB.x, newC.x) ||
                    !doubleEqual(newC.y, newD.y)) {
                throw new IllegalArgumentException("rectangle is not parallel to x an y axis:" +
                        "\nrectangle[0] = " + axisParallelRectangle[0] +
                        "\nrectangle[1] = " + axisParallelRectangle[1] +
                        "\nrectangle[2] = " + axisParallelRectangle[2] +
                        "\nrectangle[3] = " + axisParallelRectangle[3] +
                        "\nnewA = " + newA +
                        "\nnewB = " + newB +
                        "\nnewC = " + newC +
                        "\nnewD = " + newD
                );
            }
        }
        return new Coordinate[]{
                fromAxisParallel.transform(newA, new Coordinate()),
                fromAxisParallel.transform(newB, new Coordinate()),
                fromAxisParallel.transform(newC, new Coordinate()),
                fromAxisParallel.transform(newD, new Coordinate())
        };
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
