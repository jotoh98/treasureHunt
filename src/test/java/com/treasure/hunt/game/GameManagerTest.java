package com.treasure.hunt.game;

import com.treasure.hunt.jts.geom.Circle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hider.impl.RandomAngleHintHider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
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
            instance.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        instance.next(); // TODO fix
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
        public void init(Point searcherStartPosition) {

        }

        @Override
        public SearchPath move() {
            Point point = JTSUtils.createPoint(0, 0);
            lastMove = point;
            return new SearchPath(point);
        }

        /**
         * @param hint the hint, the {@link com.treasure.hunt.strategy.hider.Hider} gave last.
         * @return A {@link SearchPath}, containing a useless {@link Circle} as additional items, which should get removed.
         */
        @Override
        public SearchPath move(AngleHint hint) {
            SearchPath searchPath = new SearchPath(lastMove, lastMove);
            if (toBeRemovedLater != null) {
                searchPath.getGeometryItemsToBeRemoved().add(toBeRemovedLater);
                return searchPath;
            }
            toBeRemovedLater = new GeometryItem<>(new Circle(new Coordinate(0, 0), 20, 20, JTSUtils.GEOMETRY_FACTORY), GeometryType.STANDARD);
            searchPath.addAdditionalItem(toBeRemovedLater);

            return searchPath;
        }
    }

}