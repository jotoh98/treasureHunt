package com.treasure.hunt.analysis;

import com.treasure.hunt.game.Move;
import com.treasure.hunt.strategy.geom.GeometryItem;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Statistic {
    private List<Move> moves;

    public double getTraceLength() {
        double traceroutelength = 0;
        List<Point> listPoints = getListPoints();
        for (int i = 0; i < listPoints.size() - 1; i++) {
            traceroutelength += listPoints.get(i).distance(listPoints.get(i + 1));
        }
        return traceroutelength;
    }

    public List<Point> getListPoints() {
        List<Point> stepPoints = new ArrayList<>();
        stepPoints.add(getStartPoint());
        for (Move move : moves) {
            boolean firstElement = true;
            for (GeometryItem<Point> point : move.getMovement().getPoints()
            ) {
                if (firstElement) {
                    firstElement = false;
                } else {
                    stepPoints.add(point.getGeometry());
                }
            }
        }
        stepPoints.add(getTreasureLocation());
        return stepPoints;
    }

    public Point getStartPoint() {
        return moves.get(0).getMovement().getPoints().get(0).getGeometry();
    }

    public Point getTreasureLocation() {
        return moves.get(0).getTreasureLocation();
    }

    public double getOptimumSolution() {
        return getStartPoint().distance(getTreasureLocation());
    }

    public double getRunningTimeFactor(Function<Double, Double> method) {
        return getTraceLength() / method.apply(getOptimumSolution());
    }

    public double getLinearRunningTimeFactor() {
        return getRunningTimeFactor((Double opt) -> opt);
    }

    public double getQuadraticRunningTimeFactor() {
        return this.getRunningTimeFactor((Double opt) -> opt * opt);
    }

    public double getHintRequests() {
        return moves.size() - 1;
    }

    public double getHintTraceLengthRatio() {
        return getHintRequests() / getTraceLength();
    }

    public List<StatisticObject> calculate(List<Move> moves) {
        this.moves = moves;
        List<StatisticObject> statistics = Arrays.asList(
                new StatisticObject("Trace length",
                        "If finished: Lenght of searchers path; if unfinished: Length of searchers path plus the direct route from searchers last point to treasure",
                        getTraceLength()
                )
        );
        return statistics;
    }
}


