package com.treasure.hunt.view.utils;

import com.treasure.hunt.game.Turn;
import com.treasure.hunt.jts.geom.GeometryAngle;
import com.treasure.hunt.service.settings.Settings;
import com.treasure.hunt.service.settings.SettingsService;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryStyle;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.strategy.hint.impl.CircleHint;
import com.treasure.hunt.strategy.searcher.SearchPath;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.NonNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Constructs an object tree primarily used for the list of turns in the game.
 * The tree is formed in a recursive way by invoking {@link #getTree(Turn)}.
 * New items and leafs can be implemented by writing new <code>createItem()</code> methods according to the scheme:
 * <pre>{@code
 * public static TreeItem<String> createItem(Object object) {
 *      //choose a good root representation for the object
 *      TreeItem<String> root = [create_root_for_object];
 *
 *      //if there are members
 *      foreach(Object member: object) {
 *          root.getChildren().add(createItem(member));
 *      }
 *
 *      return root;
 * }
 * }</pre>
 *
 * @author hassel
 */
public class TreeConstructor {

    /**
     * Hidden constructor, the tree has no internal storage.
     */
    private TreeConstructor() {
    }

    /**
     * Create the tree root for a turn.
     * Its root is invisible and this roots only purpose is to hold the turns fields.
     * It utilises {@link #createItem(Turn)}.
     *
     * @param turn the object trees root turn
     * @return root tree view with the object tree of the turn
     */
    public static TreeView<String> getTree(Turn turn) {
        TreeView<String> stringTreeView = new TreeView<>(
                TreeConstructor.createItem(turn)
        );
        stringTreeView.setShowRoot(false);
        return stringTreeView;
    }

    /**
     * Create the tree item for a turn.
     * Its children are the items for the turn's fields:
     * <ul>
     *     <li>treasure location ({@link #createItem(Coordinate)})</li>
     *     <li>hint ({@link #createItem(Hint)})</li>
     *     <li>search path ({@link #createItem(SearchPath)})</li>
     * </ul>
     *
     * @param turn turn to display
     * @return root tree item for the turn object tree
     */
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

    /**
     * Get the tree item for a hint.
     * Here, we differentiate between {@link AngleHint}, {@link CircleHint}, etc. and use the corresponding methods.
     *
     * @param hint hint, which is printed in the tree item
     * @return root tree item for a hint and its members
     * @see #createItem(GeometryAngle)
     * @see #createItem(Coordinate)
     */
    public static TreeItem<String> createItem(Hint hint) {
        final TreeItem<String> root = createItem(hint.getClass().getSimpleName());
        if (hint instanceof AngleHint) {
            AngleHint angleHint = (AngleHint) hint;
            TreeItem<String> angleItem = createItem(angleHint.getGeometryAngle());
            root.getChildren().add(angleItem);
        } else if (hint instanceof CircleHint) {
            CircleHint circleHint = (CircleHint) hint;
            root.getChildren().addAll(
                    createItem("center: %s", print(circleHint.getCircle().getCenter())),
                    createItem("radius: %s", round(circleHint.getCircle().getRadius()))
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

    /**
     * Creates a tree item for a search path.
     * The children are tree items for the paths fields:
     * <ul>
     *     <li>list of way points ({@link #createItem(Coordinate)})</li>
     *     <li>list of additional geometry items ({@link #createItem(GeometryItem)})</li>
     * </ul>
     *
     * @param path search path to create the tree item for
     * @return root tree item for a search path
     * @see #createItem(Coordinate)
     * @see #createItem(GeometryItem)
     */
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

    /**
     * Creates a tree item for a geometry angle.
     * The children are tree items for the angles defining points: <i>left, center &amp; right</i> using {@link #createItem(Coordinate)}.
     *
     * @param angle angle to print as a tree item
     * @return root tree item for a geometry angle
     * @see #createItem(Coordinate)
     */
    public static TreeItem<String> createItem(GeometryAngle angle) {
        final TreeItem<String> root = createItem("GeometryAngle");

        root.getChildren().addAll(
                createItem("left: %s", print(angle.getLeft())),
                createItem("center: %s", print(angle.getCenter())),
                createItem("right: %s", print(angle.getRight()))
        );

        return root;
    }

    /**
     * Creates a tree item for a geometry item.
     * The children are tree items for the items fields:
     * <ul>
     *      <li>object ({@link #createItem(String, Object...)}, {@link #createItem(Geometry)}, {@link #createItem(LineSegment)}  )})</li>
     *      <li>geometry type ({@link #createItem(GeometryType)}</li>
     *      <li>geometry style ({@link #createItem(GeometryStyle)}</li>
     * </ul>
     *
     * @param geometryItem geometry item to print as a tree item
     * @param <T>          type of the object wrapped in the geometry item
     * @return root tree item representing a geometry item
     * @see GeometryItem
     * @see GeometryType
     * @see GeometryStyle
     */
    public static <T> TreeItem<String> createItem(GeometryItem<T> geometryItem) {
        final TreeItem<String> root = createItem("Geometry Item");

        TreeItem<String> objectRoot;
        @NonNull T geometryObject = geometryItem.getObject();
        if (geometryObject instanceof Geometry) {
            objectRoot = createItem((Geometry) geometryObject);
        } else if (geometryObject instanceof LineSegment) {
            objectRoot = createItem((LineSegment) geometryObject);
        } else {
            objectRoot = createItem(geometryObject.toString());
        }

        root.getChildren().addAll(
                objectRoot,
                createItem(geometryItem.getGeometryType()),
                createItem(geometryItem.getGeometryStyle())
        );

        return root;

    }

    /**
     * Creates a tree item for a line segment.
     * The name is derived by the {@link Class#getSimpleName()} method. The children are tree items for the two
     * line segment points {@link LineSegment#p0} and {@link LineSegment#p1}.
     *
     * @param lineSegment line segment to print as a tree item
     * @return root tree item representing a line segment
     * @see #createItem(String, Object...)
     */
    public static TreeItem<String> createItem(LineSegment lineSegment) {
        TreeItem<String> root = createItem(lineSegment.getClass().getSimpleName());
        root.getChildren().addAll(
                createItem("p0: %s", print(lineSegment.p0)),
                createItem("p1: %s", print(lineSegment.p1))
        );
        return root;
    }

    /**
     * Creates a tree item for a geometry.
     * The children are tree items for the geometry's coordinates.
     *
     * @param geometry geometry to print as a tree item
     * @return root tree item representing a geometry
     * @see #createItem(Coordinate)
     */
    public static TreeItem<String> createItem(Geometry geometry) {
        TreeItem<String> root = createItem(geometry.getGeometryType());
        for (Coordinate coordinate : geometry.getCoordinates()) {
            root.getChildren().add(createItem(coordinate));
        }
        return root;
    }

    /**
     * Creates a tree item for a geometry style.
     * The children are all the separate fields of the style.
     *
     * @param geometryStyle style to print as a tree item
     * @return root tree item representing a geometry style
     * @see #createItem(String, Object...)
     */
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

    /**
     * Creates a tree item for a geometry type.
     * The children are all the separate fields of the type.
     *
     * @param geometryType type to print as a tree item
     * @return root tree item representing a geometry type
     * @see #createItem(String, Object...)
     */
    public static TreeItem<String> createItem(GeometryType geometryType) {
        final TreeItem<String> root = createItem("GeometryType: %s", geometryType.getDisplayName());

        root.getChildren().addAll(
                createItem("enabled: %b", geometryType.isEnabled()),
                createItem("override: %b", geometryType.isOverride()),
                createItem("multi style: %b", geometryType.isMultiStyle())
        );

        return root;
    }

    /**
     * Creates a leaf tree item for a coordinate.
     *
     * @param coordinate coordinate to print as a tree item
     * @return leaf tree item representing a coordinate
     * @see #print(Coordinate)
     */
    public static TreeItem<String> createItem(Coordinate coordinate) {
        return createItem("Coordinate: %s", print(coordinate));
    }

    /**
     * Creates a leaf tree item for a format string.
     *
     * @param format a format string
     * @param args   arguments for the format string
     * @return leaf tree item representing the formatted string
     * @see String#format(String, Object...)
     */
    public static TreeItem<String> createItem(String format, Object... args) {
        return new TreeItem<>(String.format(format, args));
    }

    /**
     * Prints a coordinate with rounded
     *
     * @param c coordinate to print rounded
     * @return rounded coordinate string representation
     */
    private static String print(Coordinate c) {
        return String.format("(%s, %s)", round(c.x), round(c.y));
    }

    /**
     * Round a double to a specified decimal place.
     *
     * @param value double value to round
     * @return rounded double value
     * @see BigDecimal
     * @see Settings#getDecimalPlaces()
     */
    private static double round(double value) {
        int places = SettingsService.getInstance().getSettings().getDecimalPlaces();
        if (places < 0) {
            return value;
        }

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
