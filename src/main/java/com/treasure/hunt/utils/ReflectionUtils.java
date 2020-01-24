package com.treasure.hunt.utils;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author jotoh
 */
public class ReflectionUtils {
    public static Class interfaceGenericsClass(Class baseClass) {
        return interfaceGenericsClass(baseClass, 0);
    }

    public static Class[] interfaceGenericsClasses(Class baseClass) {
        Type[] interfaceGenericTypes = ((ParameterizedType) (baseClass.getGenericInterfaces()[0])).getActualTypeArguments();

        Class[] interfaceGenerics = new Class[interfaceGenericTypes.length];

        Arrays.fill(interfaceGenerics, null);

        for (int i = 0; i < interfaceGenericTypes.length; i++) {

            try {
                interfaceGenerics[i] = Class.forName(
                        ((Class) interfaceGenericTypes[i]).getName()
                );
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return interfaceGenerics;
    }

    public static Class interfaceGenericsClass(Class baseClass, int index) {
        Class[] genericNames = interfaceGenericsClasses(baseClass);

        if (genericNames.length < 1) {
            return null;
        }

        index %= genericNames.length;
        return genericNames[index];
    }

    public static String[] genericsNames(Class baseClass) {
        Class[] classes = interfaceGenericsClasses(baseClass);

        String[] names = new String[classes.length];

        for (int i = 0; i < classes.length; i++) {
            names[i] = classes[i].getSimpleName();
        }

        return names;
    }

    public static String genericName(Class baseClass) {
        return interfaceGenericsClass(baseClass).getSimpleName();
    }

    public static Set<Class<? extends Searcher>> getAllSearchers() {
        Reflections searcherReflections = new Reflections("com.treasure.hunt.strategy.searcher.impl");

        Set<Class<? extends Searcher>> allSearchers = searcherReflections.getSubTypesOf(Searcher.class);
        return allSearchers.stream().filter(aClass -> !Modifier.isAbstract(aClass.getModifiers())).collect(Collectors.toSet());
    }

    public static Set<Class<? extends Hider>> getAllHiders() {
        Reflections hiderReflections = new Reflections("com.treasure.hunt.strategy.hider.impl");

        Set<Class<? extends Hider>> allHiders = hiderReflections.getSubTypesOf(Hider.class);
        return allHiders.stream().filter(aClass -> !Modifier.isAbstract(aClass.getModifiers())).collect(Collectors.toSet());
    }

    public static Set<Class<? extends GameEngine>> getAllGameEngines() {
        Reflections reflections = new Reflections("com.treasure.hunt.game");
        Set<Class<? extends GameEngine>> allGameEngines = reflections.getSubTypesOf(GameEngine.class);
        allGameEngines = allGameEngines.stream().filter(aClass -> !Modifier.isAbstract(aClass.getModifiers())).collect(Collectors.toSet());
        allGameEngines.add(GameEngine.class);
        return allGameEngines;
    }
}
