package com.treasure.hunt.utils;

import java.lang.annotation.Repeatable;

@Repeatable(value = Preferences.class)
public @interface Preference {
    String name();

    double value();
}
