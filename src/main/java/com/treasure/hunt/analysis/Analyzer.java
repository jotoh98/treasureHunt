package com.treasure.hunt.analysis;

import com.treasure.hunt.game.*;
import com.treasure.hunt.strategy.geom.GeometryItem;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;


import java.util.ArrayList;
import java.util.List;
//TODO: Kind a unnecessary class the more you think about it; Should be replaced by CreatorOfInstances and RunInstanceData in the future
public class Analyzer {

    //Compare multiple different types of runs
    private static final List<RunInstancesOverview> RunInstancesOverviews=new ArrayList<>();


    public static boolean checkiffinished(){
        return true;
    }

    public static List<Point> getRunData(List<Move> moves){
        List<Point> stepPoints=new ArrayList<>();
        if(moves.size()==0){
            throw new IllegalStateException("List of Movements has size of 0.");
        }
        //extract every point (tracerroute) ordered
        for (Move move: moves
             ) {
            for (GeometryItem<Point> point : move.getMovement().getPoints()
            ) {
                stepPoints.add(point.getObject());
            }
        }
        return stepPoints;
    }

    public static double computeTraceroutelength(List<Point> stepPoints){
        double traceroutelength=0;
        Point tmp=null;
        for (Point point: stepPoints
             ) {tmp=computeDiffVector(tmp,point);
                traceroutelength+=tmp.getLength();
        }
        return traceroutelength;
        }

    public static Point computeDiffVector(Point a, Point b){
        //Return distance Vector form a Point a to b; Is zero for only one Vector
        GeometryFactory gf = new GeometryFactory();
        if(a==null){
            return gf.createPoint(new Coordinate(0,0));
        }
        return gf.createPoint(new Coordinate(a.getX()-b.getX(),a.getY()-b.getY()));

    }
    public static double getOptimumSolution(Point startpoint,Point treasureLocation){
        GeometryFactory gf=new GeometryFactory();
        return gf.createPoint(new Coordinate(startpoint.getX()-treasureLocation.getX(),startpoint.getY()-treasureLocation.getY())).getLength();
    }
    public static double getSolutionQuotient(double optSolutionValue,double traceLength){
        return traceLength/optSolutionValue;
    }

    public static RunInstanceData create_runInstanceData(List<Move> moves){
        List<Point> movePoints=getRunData(moves);
        Point treasureLocation= moves.get(0).getTreasureLocation();
        Point startpoint= moves.get(0).getMovement().getPoints().get(0).getObject();
        double tracelength = computeTraceroutelength(movePoints);
        double optSolutionValue= getOptimumSolution(startpoint,treasureLocation);
        double solutionQuotient= getSolutionQuotient(optSolutionValue,tracelength);
        //taken the assumption that every movement has a hint requested
        int hintRequests= moves.size();
        return new RunInstanceData(movePoints,tracelength,startpoint,treasureLocation,optSolutionValue,solutionQuotient,hintRequests, moves);

    }
    public static void printData(RunInstanceData printme){
        System.out.println(printme.toString());

    }

    }
