package com.treasure.hunt.utils;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

@Slf4j
public class RenderUtils {

    public static Shape shapeFromText(Font font, String string, double x, double y) {
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.translate(x, y);
        return affineTransform.createTransformedShape(shapeFromText(font, string));
    }

    public static Shape shapeFromText(Font font, String string) {
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = bufferedImage.createGraphics();

        try {
            GlyphVector glyphVector = font.createGlyphVector(graphics2D.getFontRenderContext(), string);
            return glyphVector.getOutline(0f, (float) -glyphVector.getVisualBounds().getY());
        } catch (Exception e) {
            log.warn("Could not render text", e);
        } finally {
            graphics2D.dispose();
        }
        return null;
    }
}
