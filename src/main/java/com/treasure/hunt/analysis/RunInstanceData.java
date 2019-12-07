package com.treasure.hunt.analysis;

import com.treasure.hunt.game.Move;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

import java.util.List;
@Getter
public class RunInstanceData {

    private final List<Point> steps;
    private final double traceLength;
    private final Point startpoint;
    private final Point treasureLocation;
    private final double optSolution;
    private final double solutionQuotient;
    private final int hintRequests;
    private final List<Move> actualRun;
    private final int id;

    RunInstanceData(List<Point> steps, double traceLength, Point startpoint, Point treasureLocation, double optSolution, double solutionQuotient, int hintRequests, List<Move> actualRun){
        this.steps = steps;
        this.traceLength = traceLength;
        this.startpoint = startpoint;
        this.treasureLocation = treasureLocation;
        this.optSolution = optSolution;
        this.solutionQuotient = solutionQuotient;
        this.hintRequests = hintRequests;
        this.actualRun = actualRun;
        this.id=this.idsetter();
    }

    private int idsetter(){
        return 0;
    }

    //TODO finish this in detail with the possibility to dump this into a json file
    @Override
    public String toString() {
        String output= String.format("traceLength= %f\noptimal Solution= %f\nQuotient = %f",traceLength,optSolution,solutionQuotient);
        return output;
    }
}
