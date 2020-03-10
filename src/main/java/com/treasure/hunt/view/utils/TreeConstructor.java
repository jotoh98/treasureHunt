package com.treasure.hunt.view.utils;

import com.treasure.hunt.game.Turn;
import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryStyle;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.locationtech.jts.geom.Coordinate;

public class TreeConstructor {

    public static TreeView<String> getTree(Turn turn) {
        TreeView<String> stringTreeView = new TreeView<>(
                TreeConstructor.createItem(turn)
        );
        stringTreeView.setShowRoot(false);
        return stringTreeView;
    }

    public static TreeItem<String> createItem(Turn turn) {
        final TreeItem<String> root = createItem("Turn");
        root.setExpanded(true);

        if (turn.getTreasureLocation() != null) {
            root.getChildren().add(createItem("Treasure: %s", print(turn.getTreasureLocation().getCoordinate())));
        }

        if (turn.getHint() != null) {
            root.getChildren().add(createItem(turn.getHint()));
        }

        if (turn.getSearchPath() != null) {
            root.getChildren().add(createItem(turn.getSearchPath()));
        }

        return root;
    }

    public static TreeItem<String> createItem(Hint hint) {
        final TreeItem<String> root = createItem("Hint");
        if (hint instanceof AngleHint) {
            root.setValue("AngleHint");
            AngleHint angleHint = (AngleHint) hint;
            TreeItem<String> angleItem = createItem(angleHint.getGeometryAngle());
            root.getChildren().add(angleItem);
        } else if (hint instanceof CircleHint) {
            root.setValue("CircleHint");
            CircleHint circleHint = (CircleHint) hint;

            root.getChildren().addAll(
                    createItem("center: %s", print(circleHint.getCenter().getCoordinate())),
                    createItem("radius: %s", circleHint.getRadius())
            );
        } else {
            root.setValue(hint.toString());
        }

        final TreeItem<String> additionalItems = createItem("Additional Items");

        if (hint != null) {
            hint.getAdditionalGeometryItems().forEach(
                    geometryItem -> additionalItems.getChildren().add(createItem(geometryItem))
            );
        }

        root.getChildren().add(additionalItems);

        return root;
    }

    public static TreeItem<String> createItem(SearchPath path) {

        final TreeItem<String> root = createItem("SearchPath");
        final TreeItem<String> points = createItem("Points");
        final TreeItem<String> additional = createItem("Additional Items");

        path.getPoints().forEach(
                point -> points.getChildren().add(createItem(point.getCoordinate()))
        );

        path.getAdditional().forEach(
                geometryItem -> additional.getChildren().add(createItem(geometryItem))
        );

        root.getChildren().addAll(points, additional);

        return root;
    }

    public static TreeItem<String> createItem(GeometryAngle angle) {
        final TreeItem<String> root = createItem("GeometryAngle");
        root.getChildren().addAll(
                createItem("left: %s", print(angle.getLeft())),
                createItem("center: %s", print(angle.getCenter())),
                createItem("right: %s", print(angle.getRight()))
        );

        return root;
    }

    public static TreeItem<String> createItem(Coordinate coordinate) {
        return createItem("Coordinate: %s", print(coordinate));
    }

    public static <T> TreeItem<String> createItem(GeometryItem<T> geometryItem) {
        final TreeItem<String> root = createItem("Geometry Item: %s", geometryItem.getObject().toString());

        root.getChildren().addAll(
                createItem(geometryItem.getGeometryType()),
                createItem(geometryItem.getGeometryStyle())
        );

        return root;

    }

    public static TreeItem<String> createItem(GeometryStyle geometryStyle) {
        final TreeItem<String> root = createItem("GeometryStyle");

        root.getChildren().addAll(
                createItem("visible: %b", geometryStyle.isVisible()),
                createItem("filled: %b", geometryStyle.isFilled()),
                createItem("outline color: %s", geometryStyle.getOutlineColor().toString()),
                createItem("fill color: %s", geometryStyle.getFillColor().toString()),
                createItem("z-index: %s", geometryStyle.getZIndex())
        );

        return root;
    }

    public static TreeItem<String> createItem(GeometryType geometryType) {
        final TreeItem<String> root = createItem("GeometryType: %s", geometryType.getDisplayName());

        root.getChildren().addAll(
                createItem("enabled: %b", geometryType.isEnabled()),
                createItem("override: %b", geometryType.isOverride()),
                createItem("multi style: %b", geometryType.isMultiStyle())
        );

        return root;
    }

    public static TreeItem<String> createItem(String format, Object... args) {
        return new TreeItem<>(String.format(format, args));
    }

    private static String print(Coordinate c) {
        return "(" + c.x + ", " + c.y + ")";
    }
}
