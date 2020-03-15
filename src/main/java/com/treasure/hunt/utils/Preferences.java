package com.treasure.hunt.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Preferences {
    Preference[] value() default {};
}
