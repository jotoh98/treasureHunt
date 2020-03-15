package com.treasure.hunt.service.settings;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Settings {
    @PrefName(prefName = "Preserve configuration", prefDesc = "Saves the strategy configuration after program exit.")
    private boolean preserveConfiguration = true;

    @PrefName(prefName = "Naming:", prefDesc = "Var naming prefix shit")
    private String naming = "Default value";

    @PrefName(prefName = "Counter:")
    private int counter = 15;
}
