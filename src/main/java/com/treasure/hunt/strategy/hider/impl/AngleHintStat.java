package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hint.Hint;
import com.treasure.hunt.strategy.hint.impl.AngleHint;
import com.treasure.hunt.utils.JTSUtils;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;

public class AngleHintStat {

    @Getter
    private AngleHint hint;
    @Getter@Setter
    private double absoluteAreaCutoff;
    @Getter@Setter
    private double relativeAreaCutoff;
    @Getter@Setter
    private Pair<Coordinate, Double> worstConstantPoint = new Pair(new Coordinate(0,0),  0.0);

    public AngleHintStat(AngleHint hint){
        this.hint = hint;

    }

    public AngleHintStat(AngleHint hint, double areaBefore, double areaAfter){
        this.hint = hint;
        this.absoluteAreaCutoff = areaBefore -areaAfter;
        this.relativeAreaCutoff = absoluteAreaCutoff / areaBefore;

    }

    @Override
    public String toString() {
        return "Hint at " + hint.getGeometryAngle().getCenter() + " with angle of" + Angle.toDegrees(hint.getGeometryAngle().getNormalizedAngle()) +
                ", absolute Area Cutoff=" + absoluteAreaCutoff +
                ", relative Area Cutoff= " + relativeAreaCutoff +
                ", WorstPoint is =" + worstConstantPoint.getKey() +
                " with C= " + worstConstantPoint.getValue() +
                '}';
    }

    public void setWorstConstantPoint(Coordinate c, Double constant) {
        this.worstConstantPoint = new Pair<>(c,constant);
    }

}
