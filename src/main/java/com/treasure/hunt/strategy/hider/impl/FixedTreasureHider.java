package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.geom.*;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.utils.JTSUtils;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
public class FixedTreasureHider extends StatisticalHider implements Hider<AngleHint> {

    @Override
    protected double rateHint(AngleHintStat ahs){
        double rating = 0;

        rating += 10 * ( 1 / ahs.getRelativeAreaCutoff());
        rating += 1 * ahs.getDistanceFromNormalAngleLineToTreasure();
        rating += 3 * ahs.getDistanceFromResultingCentroidToTreasure();

        ahs.setRating(rating);
        return rating;
    }

    @Override
    public Point getTreasureLocation(){
        return this.treasure;
    }
}
