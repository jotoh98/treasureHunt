package com.treasure.hunt.jts.other;

import com.treasure.hunt.jts.awt.AdvancedShapeWriter;
import com.treasure.hunt.service.io.ImageService;
import com.treasure.hunt.strategy.geom.GeometryStyle;
import com.treasure.hunt.strategy.geom.JavaFxDrawable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.locationtech.jts.geom.Coordinate;

import java.awt.*;


public class ImageItem implements JavaFxDrawable {
    private final Coordinate position;
    private final Alignment alignment;
    private final int maxWidth;
    private final int maxHeight;
    private final String imagePath;
    private final Image image;

    public ImageItem(Coordinate position, int maxWidth, int maxHeight, String imagePath, Alignment alignment) {
        this.position = new Coordinate(position.x, position.y);
        this.alignment = alignment;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.imagePath = imagePath;
        this.image = ImageService.getInstance().load(getClass().getResource(imagePath).toExternalForm(), maxWidth, maxHeight, true, true);

    }

    @Override
    public void draw(GeometryStyle geometryStyle, GraphicsContext graphics2D, AdvancedShapeWriter shapeWriter) {
        Point dest = new Point();
        double height = image.getHeight();
        double width = image.getWidth();
        shapeWriter.transform(position, dest);
        if (alignment == Alignment.BOTTOM_CENTER) {
            dest.x = (int) (dest.x - width / 2);
            dest.y = (int) (dest.y - height);
        } else if (alignment == Alignment.CENTER_CENTER) {
            dest.x = (int) (dest.x - width / 2);
            dest.y = (int) (dest.y - width / 2);
        }

        graphics2D.drawImage(image, dest.x, dest.y);
    }

    public enum Alignment {
        CENTER_CENTER,
        BOTTOM_CENTER,
    }
}
