package com.treasure.hunt.strategy.geom;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.*;

@Data
@AllArgsConstructor
public class GeometryType {
    Color color;
    boolean visibleByDefault = false;
    String displayText = "";
}
