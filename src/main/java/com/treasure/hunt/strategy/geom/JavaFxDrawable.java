package com.treasure.hunt.strategy.geom;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import javafx.scene.canvas.GraphicsContext;

public interface JavaFxDrawable {
    void draw(GeometryStyle geometryStyle, GraphicsContext graphics2D, AdvancedShapeWriter shapeWriter);
}
