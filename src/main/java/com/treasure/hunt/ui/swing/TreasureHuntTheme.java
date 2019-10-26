package com.treasure.hunt.ui.swing;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalTheme;

public class TreasureHuntTheme extends MetalTheme {

    protected static ColorUIResource darkgrey = new ColorUIResource(0x202020);

    @Override
    public String getName() {
        return "TreasureHuntTheme";
    }

    @Override
    protected ColorUIResource getPrimary1() {
        return new ColorUIResource(0x440F0A);
    }

    @Override
    protected ColorUIResource getPrimary2() {
        return new ColorUIResource(0x42440F);
    }

    @Override
    protected ColorUIResource getPrimary3() {
        return new ColorUIResource(0x17440C);
    }

    @Override
    protected ColorUIResource getSecondary1() {
        return new ColorUIResource(0x889113);
    }

    @Override
    protected ColorUIResource getSecondary2() {
        return new ColorUIResource(0x745DDF);
    }

    @Override
    protected ColorUIResource getSecondary3() {
        return darkgrey;
    }

    @Override
    public FontUIResource getControlTextFont() {
        return null;
    }

    @Override
    public FontUIResource getSystemTextFont() {
        return null;
    }

    @Override
    public FontUIResource getUserTextFont() {
        return null;
    }

    @Override
    public FontUIResource getMenuTextFont() {
        return null;
    }

    @Override
    public FontUIResource getWindowTitleFont() {
        return null;
    }

    @Override
    public FontUIResource getSubTextFont() {
        return null;
    }

    @Override
    public ColorUIResource getWindowBackground() {
        return darkgrey;

    }
}
