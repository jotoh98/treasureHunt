package com.treasure.hunt.service.settings;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.NumberFormat;

@NoArgsConstructor
@Data
public class Settings {
    @UserSetting(name = "Preserve configuration", desc = "Saves the strategy configuration after program exit.")
    private boolean preserveConfiguration = true;

    @UserSetting(name = "Decimal places", desc = "Amount of decimal places to display")
    private int decimalPlaces = 12;

    public String round(double value) {
        if (decimalPlaces < 0) {
            return Double.toString(value);
        }
        final NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(decimalPlaces);
        return numberFormat.format(value);
    }
}
