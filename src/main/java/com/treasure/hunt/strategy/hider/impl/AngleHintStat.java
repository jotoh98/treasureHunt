package com.treasure.hunt.strategy.hider.impl;

import com.treasure.hunt.strategy.hint.impl.AngleHint;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

public class AngleHintStat {

    @Getter
    private AngleHint hint;
    @Getter
    private Geometry areaBeforeHint;
    @Getter
    private Geometry areaAfterHint;
    @Getter
    private double absoluteAreaCutoff;
    @Getter
    private double relativeAreaCutoff;
    @Getter@Setter
    private double distanceFromNormalAngleLineToTreasure;
    @Getter@Setter
    private double distanceFromResultingCentroidToTreasure;
    @Getter@Setter
    private Pair<Coordinate, Double> worstConstantPoint = new Pair(new Coordinate(0,0),  0.0);
    @Getter@Setter
    double rating;


    public AngleHintStat(AngleHint hint){
        this.hint = hint;

    }

    public AngleHintStat(AngleHint hint, Geometry areaBefore, Geometry areaAfter){
        this.hint = hint;
        this.areaBeforeHint = areaBefore;
        this.areaAfterHint = areaAfter;

        calcAreaCutoffs();

    }

    private void calcAreaCutoffs(){
        double areaBeforeSize = areaBeforeHint.getArea();
        double areaAfterSize = areaAfterHint.getArea();
        this.absoluteAreaCutoff = areaBeforeSize - areaAfterSize;
        this.relativeAreaCutoff = absoluteAreaCutoff / areaBeforeSize;
    }



    @Override
    public String toString() {
        return "Hint at " + hint.getGeometryAngle().getCenter() + " with normal angle of" + Angle.toDegrees(hint.getGeometryAngle().getNormalizedAngle()) +
                ", absolute Area Cutoff=" + absoluteAreaCutoff +
                ", relative Area Cutoff= " + relativeAreaCutoff +
                ", Centroid distance to Treasure = " + distanceFromResultingCentroidToTreasure +
                ", NormalLine distance to Treasure = " + distanceFromNormalAngleLineToTreasure +
                ", WorstPoint is =" + worstConstantPoint.getKey() +
                " with C= " + worstConstantPoint.getValue() +
                '}';
    }

    public void setWorstConstantPoint(Coordinate c, Double constant) {
        this.worstConstantPoint = new Pair<>(c,constant);
    }

}
