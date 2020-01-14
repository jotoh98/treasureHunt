package com.treasure.hunt.utils;

import com.pploder.events.Event;
import com.pploder.events.SimpleEvent;
import com.treasure.hunt.analysis.StatisticsWithIdsAndPath;
import com.treasure.hunt.game.GameManager;

/**
 * This class provides the event buses.
 *
 * @author Trostorff
 */
public class EventBusUtils {
    public static final Event<GameManager> GAME_MANAGER_LOADED_EVENT = new SimpleEvent<>();
    public static final Event<String> LOG_LABEL_EVENT = new SimpleEvent<>();
    public static final Event<StatisticsWithIdsAndPath> STATISTICS_LOADED_EVENT = new SimpleEvent<>();
}
