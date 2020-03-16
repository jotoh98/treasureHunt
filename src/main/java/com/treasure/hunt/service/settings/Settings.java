package com.treasure.hunt.service.settings;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Settings {
    @PrefName(prefName = "Preserve configuration", prefDesc = "Saves the strategy configuration after program exit.")
    private boolean preserveConfiguration = true;

    @PrefName(prefName = "Decimal places", prefDesc = "Amount of decimal places to display")
    private int decimalPlaces = 12;
}
