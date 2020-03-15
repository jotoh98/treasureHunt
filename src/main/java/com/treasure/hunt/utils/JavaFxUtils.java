package com.treasure.hunt.utils;

import org.jetbrains.annotations.NotNull;

public class JavaFxUtils {

    @NotNull
    public static String getVersionString() {
        String implementationVersion = JavaFxUtils.class.getPackage().getImplementationVersion();
        return implementationVersion == null ? "snapshot" : "v" + implementationVersion;
    }


}
