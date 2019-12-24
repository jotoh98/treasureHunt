package com.treasure.hunt.utils;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

@Slf4j
public class RenderUtils {

    public static Shape shapeFromText(Font font, String string, double x, double y) {
        AffineTransform tx = new AffineTransform();
        tx.translate(x, y);
        return tx.createTransformedShape(shapeFromText(font, string));
    }

    public static Shape shapeFromText(Font font, String string) {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        try {
            GlyphVector vect = font.createGlyphVector(g2.getFontRenderContext(), string);
            return vect.getOutline(0f, (float) -vect.getVisualBounds().getY());
        } catch (Exception e) {
            log.warn("Could not render text", e);
        } finally {
            g2.dispose();
        }
        return null;
    }
}
