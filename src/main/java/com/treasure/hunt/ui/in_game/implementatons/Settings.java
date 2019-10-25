package com.treasure.hunt.ui.in_game.implementatons;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Settings {
    boolean visibleByDefault = true;
    Color lineColor = Color.black;
    Color fillColor = Color.black;
    boolean filled = false;
    String displayText = "";


    public Settings(boolean visibleByDefault, Color lineColor, String displayText) {
        this(visibleByDefault, lineColor, Color.black, false, displayText);
    }

    public Settings(boolean visibleByDefault, Color lineColor) {
        this(visibleByDefault, lineColor, "");
    }

    public Settings(boolean visibleByDefault, Color lineColor, Color fillColor) {
        this(visibleByDefault, lineColor, fillColor, true, "");
    }

}