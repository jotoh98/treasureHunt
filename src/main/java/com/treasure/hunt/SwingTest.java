package com.treasure.hunt;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.geom.Circle;
import com.treasure.hunt.geom.Line;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.hider.impl.RandomAngleHintHider;
import com.treasure.hunt.strategy.searcher.impl.NaiveAngleSearcher;
import com.treasure.hunt.view.in_game.View;
import com.treasure.hunt.view.in_game.impl.CanvasView;
import com.treasure.hunt.view.main.CanvasController;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;

import java.util.Collections;
import java.util.List;

@Slf4j
public class SwingTest {
    public static GeometryItem[] exampleGeometryItems() {
        GeometryFactory geometryFactory = new GeometryFactory();

        Circle c1 = new Circle(new Coordinate(100, 100), 50, geometryFactory);
        Circle c2 = new Circle(new Coordinate(150, 100), 50, geometryFactory);
        Polygon intersection = (Polygon) c1.intersection(c2);

        AffineTransformation affineTransformation = new AffineTransformation().translate(0.0, 20.0);

        intersection = (Polygon) affineTransformation.transform(intersection);

        Polygon originPoint = (Polygon) affineTransformation.transform(c1);

        Point p1 = geometryFactory.createPoint(new Coordinate(0, 0));
        return new GeometryItem[]{
                new GeometryItem<>(c1),
                new GeometryItem<>(c2),
                new GeometryItem<>(originPoint),
                new GeometryItem<>(intersection),
                new GeometryItem<>(p1)
        };
    }

    public static void main(String[] args) {
        uiTest();
    }

    public static void uiTest() {
        CanvasView canvasView = new CanvasView();
        List<View> views = Collections.singletonList(canvasView);
        GameManager gameManager = null;
        try {
            gameManager = new GameManager(NaiveAngleSearcher.class, RandomAngleHintHider.class,
                    GameEngine.class, views);
        } catch (Exception e) {
            log.error("Something went wrong creating Game Manager instance. Honestly you dont need a logger msg in a " +
                    "simple swing test but Alex told me to do so. HELP", e);
        }
        CanvasController canvasController = new CanvasController(canvasView, gameManager);

        canvasView.addGeometryItem(new GeometryItem(new Line(0, 0, 0, 1)));

    }
}
