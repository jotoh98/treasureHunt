package com.treasure.hunt.strategy.searcher.impl.strategyFromPaper;

import com.treasure.hunt.strategy.hint.impl.HalfPlaneHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.geom.util.NoninvertibleTransformationException;

import java.util.Arrays;

public class MinimumRectangleStrategy implements Searcher<HalfPlaneHint> {

    Point searcherStartPosition;
    private StrategyFromPaper strategyFromPaper;
    private boolean firstMoveWithHint = true;

    private AffineTransformation fromPaper;
    private AffineTransformation forPaper;

    /**
     * @param searcherStartPosition the {@link Searcher} starting position,
     *                              he will initialized on.
     */
    @Override
    public void init(Point searcherStartPosition) {
        strategyFromPaper = new StrategyFromPaper();
        strategyFromPaper.init(JTSUtils.createPoint(0, 0));
        this.searcherStartPosition = searcherStartPosition;
    }

    /**
     * Use this to perform a initial move, without a hint given.
     * This is for the case, the searcher starts. (as he does normally)
     *
     * @return {@link Movement} the {@link Movement} the searcher did
     */
    @Override
    public Movement move() {
        return strategyFromPaper.move();
    }

    /**
     * @param hint the hint, the {@link Hider} gave last.
     * @return {@link Movement} the {@link Movement}, this searcher chose.
     */
    @Override
    public Movement move(HalfPlaneHint hint) {
        if (firstMoveWithHint) {
            firstMoveWithHint = false;

            double sinHintAngle = hint.getRight().y - hint.getCenter().y;
            double cosHintAngle = hint.getRight().x - hint.getCenter().x;
            fromPaper = AffineTransformation.rotationInstance(sinHintAngle, cosHintAngle);
            try {
                forPaper = fromPaper.getInverse();
            } catch (NoninvertibleTransformationException e) {
                throw new RuntimeException("Matrix was not invertible " + Arrays.toString(fromPaper.getMatrixEntries()),
                        e);
            }
            return transformFromPaper(strategyFromPaper.move(
                    new HalfPlaneHint(new Coordinate(0, 0), new Coordinate(1, 0))
            )); // the initial input hint for the strategy from the paper by definition shows upwards (in this strategy)
        }
        //TODO
        return null;
    }

    private Coordinate transformForPaper(double x, double y) {
        return forPaper.transform(new Coordinate(
                x - searcherStartPosition.getX(),
                y - searcherStartPosition.getY()), new Coordinate());
    }

    private Coordinate transformForPaper(Coordinate c) {
        return transformForPaper(c.x, c.y);
    }

    private HalfPlaneHint transformForPaper(HalfPlaneHint hint) {
        HalfPlaneHint lastHint = null;
        if (hint.getLastHint() != null) {
            lastHint = new HalfPlaneHint(
                    forPaper.transform(
                            transformForPaper(hint.getLastHint().getCenter()),
                            new Coordinate()
                    ),
                    forPaper.transform(
                            transformForPaper(hint.getLastHint().getRight()),
                            new Coordinate())
            );
        }

        HalfPlaneHint outputHint = new HalfPlaneHint(
                forPaper.transform(
                        transformForPaper(hint.getCenter()),
                        new Coordinate()
                ),
                forPaper.transform(
                        transformForPaper(hint.getRight()),
                        new Coordinate()),
                lastHint
        );

        return outputHint;
    }

    private Movement transformFromPaper(Movement move) {

    }
}
