package com.treasure.hunt.analysis;

import lombok.Value;

/**
 * This defines a component, we want to analyze in our simulation.
 *
 * @author Trostorff, Daniel
 */
@Value
public class StatisticObject {
    String title;
    String description;
    Object value;
}
