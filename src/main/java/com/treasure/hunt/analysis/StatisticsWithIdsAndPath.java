package com.treasure.hunt.analysis;

import lombok.Value;

import java.nio.file.Path;
import java.util.List;

@Value
public class StatisticsWithIdsAndPath {
    Path file;
    List<StatisticsWithId> statisticsWithIds;
}
