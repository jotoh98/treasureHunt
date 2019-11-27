package com.treasure.hunt.geom;

import com.treasure.hunt.jts.PointTransformation;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

@AllArgsConstructor
@Data
public class GeometryAngle implements Shapeable {
    Coordinate center;
    Coordinate left;
    Coordinate right;

    public double rightAngle() {
        return Angle.angle(center, right);
    }

    public double leftAngle() {
        return Angle.angle(center, left);
    }

    public double toRadians() {
        return leftAngle() - rightAngle();
    }

    @Override
    public Shape toShape(PointTransformation pointTransformation) {
        GeneralPath generalPath = new GeneralPath();

        Point2D destMiddle = new Point2D.Double();
        pointTransformation.transform(getCenter(), destMiddle);

        double middleX = destMiddle.getX();
        double middleY = destMiddle.getY();

        Vector2D leftVector = new Vector2D(getCenter(), getLeft());
        Vector2D rightVector = new Vector2D(getCenter(), getRight());

        leftVector = leftVector.normalize().multiply(100);
        rightVector = rightVector.normalize().multiply(100);

        generalPath.moveTo(
                middleX + leftVector.getX(),
                middleY - leftVector.getY()
        );
        generalPath.lineTo(
                middleX,
                middleY
        );
        generalPath.lineTo(
                middleX + rightVector.getX(),
                middleY - rightVector.getY()
        );

        double extend = leftAngle() - rightAngle();

        if (leftAngle() < rightAngle()) {
            extend += 2 * Math.PI;
        }

        Arc2D arc = new Arc2D.Double(
                middleX - 50,
                middleY - 50,
                100,
                100,
                Math.toDegrees(rightAngle()),
                Math.toDegrees(extend),
                Arc2D.OPEN
        );

        generalPath.append(arc, false);

        return generalPath;
    }
}
