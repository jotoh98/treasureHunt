package com.treasure.hunt.utils;

import com.pploder.events.Event;
import com.pploder.events.SimpleEvent;
import com.treasure.hunt.game.GameManager;

public class EventBusUtils {
    public static final Event<GameManager> GAME_MANAGER_LOADED_EVENT = new SimpleEvent<>();
}
