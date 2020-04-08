package com.treasure.hunt.service.settings;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.NumberFormat;
import java.util.Locale;

@NoArgsConstructor
@Data
public class Settings {
    @UserSetting(name = "Preserve configuration", desc = "Saves the strategy configuration after program exit.")
    private boolean preserveConfiguration = true;

    @UserSetting(name = "Decimal places", desc = "Amount of decimal places to display")
    private int decimalPlaces = 12;

    @UserSetting(name = "Round size", desc = "Amount of runs to execute between each save in mass runs")
    private int smallRoundSize = 500;

    @UserSetting(name = "Locale", desc = "Localization of number format")
    private Locale locale = Locale.US;

    @UserSetting(name = "Mini map scroll to screen center", desc = "Let the mini map scroll to the center of screen or to the cursors position.")
    private boolean miniMapScrollCenter = true;

    @UserSetting(name = "Mini map draggable", desc = "Make the mini map window draggable or let the screen directly jump to the clicked position.")
    private boolean miniMapDragged = true;

    public String round(double value) {
        if (decimalPlaces < 0) {
            return Double.toString(value);
        }
        final NumberFormat format = getFormat();
        format.setMaximumFractionDigits(decimalPlaces);
        return format.format(value);
    }

    public NumberFormat getFormat() {
        return NumberFormat.getInstance(locale);
    }
}
