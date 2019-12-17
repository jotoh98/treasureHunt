package com.treasure.hunt.strategy.searcher.impl;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Point;

/**
 * This {@link Searcher} runs in (by 1 growing) square-shaped circles around the
 * initial spawnpoint (0,0), until he finds the treasure.
 *
 * @author dorianreineccius
 */
public class BruteForceSearcher implements Searcher<Hint> {
    private static final int LIMIT = 1;
    private int lineSegmentDistance = 0;
    private int x = 0, y = 0;

    public void init(Point startPosition, double insecurity) {
        init(startPosition);
    }

    @Override
    public void init(Point startPosition) {
        this.lineSegmentDistance = 0;
        this.x = (int) startPosition.getX();
        this.y = (int) startPosition.getY();
    }

    @Override
    public Movement move() {
        Movement movement = new Movement(JTSUtils.createPoint(x, y));
        for (int i = 0; i < LIMIT; i++) {
            lineSegmentDistance++;
            // up
            y += lineSegmentDistance;
            movement.addWayPoint(JTSUtils.createPoint(x, y));
            // right
            x += lineSegmentDistance;
            movement.addWayPoint(JTSUtils.createPoint(x, y));

            lineSegmentDistance++;
            //down
            y -= lineSegmentDistance;
            movement.addWayPoint(JTSUtils.createPoint(x, y));
            //left
            x -= lineSegmentDistance;
            movement.addWayPoint(JTSUtils.createPoint(x, y));
        }
        for (int i = 0; i < movement.getPoints().size() - 1; i++) {
            movement.addAdditionalItem(
                    new GeometryItem(
                            JTSUtils.createLineString(
                                    JTSUtils.createPoint(
                                            movement.getPoints().get(i).getObject().getX(),
                                            movement.getPoints().get(i).getObject().getY()
                                    ),
                                    JTSUtils.createPoint(
                                            movement.getPoints().get(i + 1).getObject().getX(),
                                            movement.getPoints().get(i + 1).getObject().getY()
                                    )
                            )
                    ));
        }
        return movement;
    }

    @Override
    public Movement move(Hint hint) {
        return move();
    }
}
