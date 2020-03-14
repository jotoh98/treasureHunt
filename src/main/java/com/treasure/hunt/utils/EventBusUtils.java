package com.treasure.hunt.utils;

import com.pploder.events.Event;
import com.pploder.events.SimpleEvent;
import com.treasure.hunt.analysis.StatisticsWithIdsAndPath;
import com.treasure.hunt.game.GameManager;
import com.treasure.hunt.strategy.geom.GeometryItem;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import org.locationtech.jts.geom.Geometry;

/**
 * This class provides the event buses.
 *
 * @author Trostorff
 */
public class EventBusUtils {
    public static final Event<GameManager> GAME_MANAGER_LOADED_EVENT = new SimpleEvent<>();
    public static final Event<String> LOG_LABEL_EVENT = new SimpleEvent<>();
    public static final Event<StatisticsWithIdsAndPath> STATISTICS_LOADED_EVENT = new SimpleEvent<>();
    public static final Event<GeometryItem<? extends Geometry>> GEOMETRY_ITEM_SELECTED = new SimpleEvent<>();
    public static final Event<Node> INNER_POP_UP_EVENT = new SimpleEvent<>();
    public static final Event<MouseEvent> POP_UP_POSITION = new SimpleEvent<>();
    public static final Event<Void> INNER_POP_UP_EVENT_CLOSE = new SimpleEvent<>();
}
