package com.treasure.hunt.service.settings;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Settings {
    @UserSetting(name = "Preserve configuration", desc = "Saves the strategy configuration after program exit.")
    private boolean preserveConfiguration = true;

    @UserSetting(name = "Decimal places", desc = "Amount of decimal places to display")
    private int decimalPlaces = 12;
}
