package com.treasure.hunt.analysis;



import lombok.Getter;
import lombok.experimental.Delegate;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

public class RunInstancesOverview implements Comparable<RunInstancesOverview>{
    @Delegate
    private List<RunInstanceData> run = new ArrayList<RunInstanceData>();

    public RunInstancesOverview(List<RunInstanceData> run) {

        this.run = run;
    }

    //TODO: implement methods analysing a series of runInstances such as median of the series or minimal linear factor of the approx. solution

    //small helpers to keep it dry as your hoe
    private double getAverage(Function<RunInstanceData, Double> method) {
        if (this.size() == 0) {
            return 0;
        }
        double sum = this.run.stream().mapToDouble(method::apply).sum();
        return sum / this.size();

    }

    protected double getRunningTimeFactor(Function<Double, Double> method) {
        RunInstanceData worst = this.getWorstCase();
        return worst.getRunningTimeFactor(method);

    }

    protected double getRunningTimeFactor(Function<Double, Double> method, RunInstanceData instanceData) {
        return instanceData.getRunningTimeFactor(method);

    }

    //end small helpers

    public double getAverageTraceLength() {
        return getAverage(RunInstanceData::getTraceLength);
    }

    public double getAverageSolutionQuotient() {
        return getAverage(RunInstanceData::getLinearRunningTimeFactor);

    }

    public double getAverageHintRequest() {
        return getAverage((RunInstanceData data) -> (double) data.getHintRequests());
    }

    public void sortBy(Comparator<RunInstanceData> method) {
        this.run.sort(method);
    }

    public void sortByWorstCase() {
        sortBy(RunInstanceData::compareByRunningTime);

    }

    public void sortByBestCase() {
        sortByWorstCase();
        Collections.reverse(this.run);
    }

    public RunInstanceData getWorstCase() {

        return this.run.stream().max(RunInstanceData::compareByRunningTime).orElse(null);

    }

    public RunInstanceData getBestCase() {
        return this.run.stream().min(RunInstanceData::compareByRunningTime).orElse(null);
    }

    public RunInstancesOverview filterByOptSol(double toleranceValue) {
        List<RunInstanceData> filtered = this.run.stream()
                .filter(instanceData -> instanceData.getOptSolution() < toleranceValue)
                .collect(Collectors.toList());
        return new RunInstancesOverview(filtered);
    }

    @Override
    public int compareTo(RunInstancesOverview runInstancesOverview) {
        return 0;
    }

    //@Override add implements SortableList
    public void sort() {
        sortBy(RunInstanceData::compareByRunningTime);
    }

    public int compareBy(Function<RunInstancesOverview, Double> method, RunInstancesOverview overview) {
        return (int) Math.signum(method.apply(this) - method.apply(overview));
    }

    public int compareByAvgTraceLength(RunInstancesOverview overview) {
        return compareBy(RunInstancesOverview::getAverageTraceLength, overview);
    }

    public int compareByAvgSolutionQuotient(RunInstancesOverview overview) {
        return compareBy(RunInstancesOverview::getAverageSolutionQuotient, overview);
    }

    public int compareByAvgHintRequests(RunInstancesOverview overview) {
        return compareBy(RunInstancesOverview::getAverageHintRequest, overview);
    }


}