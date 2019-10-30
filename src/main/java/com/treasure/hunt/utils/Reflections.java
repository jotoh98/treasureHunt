package com.treasure.hunt.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public class Reflections {
    public static Class interfaceGenericsClass(Class baseClass) {
        return interfaceGenericsClass(baseClass, 0);
    }

    public static Class[] interfaceGenericsClasses(Class baseClass) {
        Type[] interfaceGenericTypes = ((ParameterizedType) (baseClass.getGenericInterfaces()[0])).getActualTypeArguments();

        Class[] interfaceGenerics = new Class[interfaceGenericTypes.length];

        Arrays.fill(interfaceGenerics, null);

        for (int i = 0; i < interfaceGenericTypes.length; i++)
            try {
                interfaceGenerics[i] = Class.forName(
                        ((Class) interfaceGenericTypes[i]).getName()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

        return interfaceGenerics;
    }

    public static Class interfaceGenericsClass(Class baseClass, int index) {
        Class[] genericNames = interfaceGenericsClasses(baseClass);

        if (genericNames.length < 1)
            return null;

        index %= genericNames.length;
        return genericNames[index];
    }

    public static String[] genericsNames(Class baseClass) {
        Class[] classes = interfaceGenericsClasses(baseClass);

        String[] names = new String[classes.length];

        for (int i = 0; i < classes.length; i++)
            names[i] = classes[i].getSimpleName();

        return names;
    }

    public static String genericName(Class baseClass) {
        return interfaceGenericsClass(baseClass).getSimpleName();
    }
}
