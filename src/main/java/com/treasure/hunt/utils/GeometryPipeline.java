package com.treasure.hunt.utils;

import com.treasure.hunt.strategy.geom.GeometryItem;
import com.treasure.hunt.strategy.geom.GeometryType;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GeometryPipeline {

    /**
     * Sort the geometry items by their z-index.
     *
     * @param stream stream of geometry items
     * @return stream of geometry items sorted by their z-index
     */
    public static Stream<GeometryItem<?>> sortZIndex(Stream<GeometryItem<?>> stream) {
        return stream.sorted(Comparator.comparingInt(o -> o.getGeometryStyle().getZIndex()));
    }

    /**
     * Filter overridden geometry items.
     *
     * @param stream stream of geometry items
     * @return stream without overridden geometry items
     */
    public static Stream<GeometryItem<?>> filterOverride(Stream<GeometryItem<?>> stream) {
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
    public static Stream<GeometryItem<?>> assignMultiStyles(Stream<GeometryItem<?>> stream) {
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
     * Combine and apply all filters on the geometry items.
     *
     * @param stream geometry items to be filtered
     * @return filtered geometry items
     */
    public static Stream<GeometryItem<?>> pipe(Stream<GeometryItem<?>> stream) {
        return ListUtils.filterStream(
                stream,
                GeometryPipeline::filterOverride,
                GeometryPipeline::assignMultiStyles,
                GeometryPipeline::sortZIndex
        );
    }
}
