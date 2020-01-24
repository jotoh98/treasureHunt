package com.treasure.hunt.analysis;

import lombok.Value;

import java.util.List;

@Value
public class StatisticsWithId {
    int id;
    List<StatisticObject> statisticObjects;
}
