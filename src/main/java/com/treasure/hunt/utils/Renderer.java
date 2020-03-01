package com.treasure.hunt.utils;

import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.game.Move;
import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.jts.awt.CanvasBoundary;
import com.treasure.hunt.jts.awt.PointTransformation;
import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryStyle;
import com.treasure.hunt.strategy.geom.GeometryType;
import com.treasure.hunt.strategy.geom.JavaFxDrawable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.jfree.fx.FXGraphics2D;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The renderer responsible for drawing {@link GeometryItem}s on the {@link Canvas}.
 * It holds an {@link AdvancedShapeWriter} for enhanced shape writing.
 */
public class Renderer {

    private HashMap<String, GeometryItem<?>> additional = new HashMap<>();

    /**
     * The jfree.fx graphics context to translate jts awt {@link Shape}s into
     * javafx {@link javafx.scene.shape.Shape}s.
     *
     * @see FXGraphics2D
     */
    private FXGraphics2D graphics2D;

    /**
     * Advanced shape writer.
     */
    private AdvancedShapeWriter shapeWriter;

    /**
     * Graphics context for native javafx {@link javafx.scene.shape.Shape}s.
     */
    private GraphicsContext context;


    /**
     * Construct the renderer held by {@link com.treasure.hunt.view.CanvasController}.
     * The given {@link GraphicsContext} forms the awt translator.
     * The {@link org.locationtech.jts.awt.ShapeWriter} gets linked to a new {@link CanvasBoundary}.
     *
     * @param context        the native context
     * @param transformation the point transformation for {@link org.locationtech.jts.geom.Geometry} instances
     */
    public Renderer(GraphicsContext context, PointTransformation transformation) {
        graphics2D = new FXGraphics2D(context);
        this.context = context;
        this.shapeWriter = new AdvancedShapeWriter(transformation);
        shapeWriter.setBoundary(new CanvasBoundary(context.getCanvas(), transformation));
    }

    /**
     * Get visible geometry items.
     * The visible {@link Move}s determine which {@link GeometryItem} are visible.
     *
     * @param stream    stream of moves
     * @param viewIndex the current max index of visible moves
     * @return stream of visible geometry items
     */
    private static Stream<GeometryItem<?>> visibleGeometries(Stream<Move> stream, int viewIndex) {
        return stream.limit(viewIndex + 1)
                .flatMap(move -> move.getGeometryItems().stream());
    }

    /**
     * Sort the geometry items by their z-index.
     *
     * @param stream stream of geometry items
     * @return stream of geometry items sorted by their z-index
     */
    private static Stream<GeometryItem<?>> sortZIndex(Stream<GeometryItem<?>> stream) {
        return stream.sorted(Comparator.comparingInt(o -> o.getGeometryStyle().getZIndex()));
    }

    /**
     * Filter overridden geometry items.
     *
     * @param stream stream of geometry items
     * @return stream without overridden geometry items
     */
    private static Stream<GeometryItem<?>> filterOverride(Stream<GeometryItem<?>> stream) {
        Map<GeometryType, List<GeometryItem<?>>> itemsByType = stream.collect(Collectors.groupingBy(GeometryItem::getGeometryType));
        return itemsByType.keySet().stream().flatMap(geometryType -> {
            List<GeometryItem<?>> itemsOfType = itemsByType.get(geometryType);
            if (geometryType.isOverride() && !geometryType.isMultiStyle()) {
                return Stream.of(itemsOfType.get(itemsOfType.size() - 1));
            }
            return itemsOfType.stream();
        });
    }

    /**
     * Assign the correct style if multiple styles are set.
     *
     * @param stream stream of geometry items
     * @return stream with correctly assigned styles
     */
    private static Stream<GeometryItem<?>> assignMultiStyles(Stream<GeometryItem<?>> stream) {
        Map<GeometryType, List<GeometryItem<?>>> itemsByType = stream.collect(Collectors.groupingBy(GeometryItem::getGeometryType));
        return itemsByType.keySet()
                .stream()
                .flatMap(geometryType -> {
                    List<GeometryItem<?>> geometryItems = itemsByType.get(geometryType);
                    int size = geometryItems.size();

                    Stream<GeometryItem<?>> geometryStream = geometryItems.stream();

                    if (size == 1 || !geometryType.isMultiStyle()) {
                        return geometryStream;
                    }
                    if (size > 1) {
                        int stylesSize = geometryItems.get(0).getGeometryStyles().size();
                        return IntStream.range(size - stylesSize, size)
                                .mapToObj(i -> {
                                    GeometryItem<?> item = geometryItems.get(i);
                                    item.setPreferredStyle(size - i);
                                    return item;
                                });
                    }
                    return Stream.empty();
                });
    }

    /**
     * Render a {@link GameManager} state.
     *
     * @param gameManager the game manager to be rendered
     */
    public void render(GameManager gameManager) {
        int viewIndex = gameManager.getViewIndex().get();
        ArrayList<Move> moves = new ArrayList<>(gameManager.getMoves());
        render(moves, viewIndex);
    }

    /**
     * Render a list of moves and additional items.
     * Filters and sorts the assigned {@link GeometryItem}s.
     *
     * @param moves     all moves available
     * @param viewIndex the current view index
     */
    private void render(List<Move> moves, int viewIndex) {
        clear();

        Stream<GeometryItem<?>> visible = visibleGeometries(moves.stream(), viewIndex);
        Stream<GeometryItem<?>> additionalStream = additional.values().stream();
        Stream<GeometryItem<?>> itemStream = Stream.concat(visible, additionalStream);

        applyFilters(
                itemStream,
                Renderer::filterOverride,
                Renderer::assignMultiStyles,
                Renderer::sortZIndex
        ).forEach(this::render);
    }

    /**
     * Render a single {@link GeometryItem}.
     *
     * @param item geometry item to be rendered
     */
    private void render(GeometryItem<?> item) {

        GeometryStyle geometryStyle = item.getGeometryStyle();

        if (!geometryStyle.isVisible()) {
            return;
        }

        Object object = item.getObject();

        if (object instanceof JavaFxDrawable) {
            render((JavaFxDrawable) object, geometryStyle);
        } else {
            render(object, geometryStyle);
        }

    }

    /**
     * Render an unspecified object with it's associated geometry style.
     *
     * @param object        object that may be rendered
     * @param geometryStyle the associated geometry style
     */
    private void render(Object object, GeometryStyle geometryStyle) {
        Shape shape = shapeWriter.toShape(object);

        if (geometryStyle.isFilled()) {
            graphics2D.setColor(geometryStyle.getFillColor());
            graphics2D.fill(shape);
        }

        graphics2D.setPaint(geometryStyle.getOutlineColor());
        graphics2D.setStroke(geometryStyle.getStroke());
        graphics2D.draw(shape);
    }

    /**
     * Render an {@link JavaFxDrawable} with it's associated geometry style.
     *
     * @param drawable the drawable to be rendered
     * @param style    the associated geometry style
     */
    private void render(JavaFxDrawable drawable, GeometryStyle style) {
        drawable.draw(style, context, shapeWriter);
    }

    /**
     * Clear up the canvas.
     */
    private void clear() {
        final Canvas canvas = context.getCanvas();
        context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Apply some stream filters to a stream.
     *
     * @param input   stream to be filtered
     * @param filters multiple filters to be applied
     * @return filtered input
     */
    @SafeVarargs
    private Stream<GeometryItem<?>> applyFilters(Stream<GeometryItem<?>> input, Function<Stream<GeometryItem<?>>, Stream<GeometryItem<?>>>... filters) {
        return Arrays.stream(filters)
                .reduce(Function::andThen)
                .orElse(Function.identity())
                .apply(input);
    }

    /**
     * Add an additional {@link GeometryItem} to the rendering queue.
     *
     * @param key  name of the additional item
     * @param item the additional item
     */
    public void addAdditional(String key, GeometryItem<?> item) {
        additional.put(key, item);
    }

    /**
     * Remove an additional {@link GeometryItem} from the rendering queue.
     *
     * @param key name of the additional item to be removed
     */
    public void removeAdditional(String key) {
        additional.remove(key);
    }
}
