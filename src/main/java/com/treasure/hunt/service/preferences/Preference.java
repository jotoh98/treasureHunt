package com.treasure.hunt.service.preferences;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(value = Preferences.class)
public @interface Preference {
    String name();

    double value();
}
