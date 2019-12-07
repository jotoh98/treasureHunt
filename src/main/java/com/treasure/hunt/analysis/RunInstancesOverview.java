package com.treasure.hunt.analysis;

import lombok.experimental.Delegate;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RunInstancesOverview {
    @Delegate
    private List<RunInstanceData> run=new ArrayList<RunInstanceData>();

    public RunInstancesOverview(List<RunInstanceData> run) {

        this.run = run;
    }

    //TODO: implement methods analysing a series of runInstances such as median of the series or minimal linear factor of the approx. solution

    //small helpers to keep it dry as your hoe
    protected double getAverage(Function<RunInstanceData, Double> method){
       double sum = this.stream().mapToDouble(method::apply).sum();
       return sum/this.size();

    }

    protected double getRunningTimeFactor(Function<Double, Double> method){
        RunInstanceData worst=this.getWorstCase();
        return worst.getTraceLength()/method.apply(getWorstCase().getOptSolution());

    }

    protected double getRunningTimeFactor(Function<Double, Double> method,RunInstanceData instanceData){
        return instanceData.getTraceLength()/method.apply(getWorstCase().getOptSolution());

    }

    protected List getListRunningTimeFactors(Function<Double, Double> method){
        List<Double> factors=new ArrayList<Double>();
        this.forEach(instance ->{
            factors.add(this.getRunningTimeFactor(method,instance));
        });
        return factors;
    }
    //end small helpers

    public double getAveragetraceLength(){
        return getAverage((RunInstanceData data)-> data.getTraceLength());
    }

    public double getAverageSolutionQuotient(){
        return getAverage((RunInstanceData data)-> data.getSolutionQuotient());

    }

    public double getAverageHintRequest(){
        return getAverage((RunInstanceData data)-> (double) data.getHintRequests());
    }

    public void sortbyWorstCase(){
        Collections.sort(this.run,((x, y) -> ((Double)x.getSolutionQuotient()).compareTo(y.getSolutionQuotient()) ));

    }

    public void sortbyBestCase(){
        this.sortbyWorstCase();
        Collections.reverse(this.run);
    }

    public RunInstanceData getWorstCase() {
        final RunInstanceData[] worst = {this.get(0)};
        if(worst[0] ==null) return null;//handle me
        this.forEach(instance ->{
            if (instance.getSolutionQuotient()< worst[0].getSolutionQuotient())
                worst[0] =instance;
        });
        return worst[0];
    }

    public double getLinearRunningTimeFactor() {
        return this.getRunningTimeFactor((Double opt)->opt);
    }

    public double getQuadraticRunningTimeFactor(){
        return this.getRunningTimeFactor((Double opt)->opt*opt);
    }

    public List getListLinearRunningTimeFactors(){
        return this.getListRunningTimeFactors((Double opt)->opt);
    }

    public RunInstancesOverview filterbyoptSol(double toleranceValue){
         List<RunInstanceData> filtered= this.run.stream()
                 .filter(instanceData -> instanceData.getOptSolution() < toleranceValue)
                 .collect(Collectors.toList());
         return new RunInstancesOverview(filtered);
    }


    //TODO add compare functions, which can compare two or more RunInstancesOverviews with eachother

}