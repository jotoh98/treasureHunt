package com.treasure.hunt.game;

import com.treasure.hunt.geom.Circle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hider.impl.RandomAngleHintHider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.utils.JTSUtils;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameManagerTest {
    private static GeometryItem<Circle> toBeRemovedLater;

    /**
     * Tests whether after deleting an item it is filtered out correctly
     */
    @Test
    public void testRemoveGeometryItems() {
        GameManager instance;
        try {
            instance = new GameManager(DeletingSearcher.class, RandomAngleHintHider.class, GameEngine.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        instance.next();
        instance.next();

        assertTrue(instance.getGeometryItems(true).contains(toBeRemovedLater));

        instance.next();
        assertFalse(instance.getGeometryItems(true).contains(toBeRemovedLater));
    }

    static class DeletingSearcher implements Searcher<AngleHint> {
        private Point lastMove;

        public DeletingSearcher() {
        }


        @Override
        public void init(Point searcherStartPosition, int width, int height) {

        }

        @Override
        public Movement move() {
            Point point = JTSUtils.createPoint(0, 0);
            lastMove = point;
            return new Movement(point);
        }

        @Override
        public Movement move(AngleHint hint) {
            Movement movement = new Movement(lastMove, lastMove);
            if (toBeRemovedLater != null) {
                movement.getToBeRemoved().add(toBeRemovedLater);
                return movement;
            }
            toBeRemovedLater = new GeometryItem<>(new Circle(new Coordinate(0, 0), 20, 20, JTSUtils.GEOMETRY_FACTORY), GeometryType.STANDARD);
            movement.addAdditionalItem(toBeRemovedLater);

            return movement;
        }
    }

}