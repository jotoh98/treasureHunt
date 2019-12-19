package com.treasure.hunt.analysis;

import lombok.experimental.Delegate;

import java.util.*;
import java.util.function.Function;

import java.util.stream.Collectors;

public class RunDataList implements Comparable<RunDataList> {
    @Delegate
    private List<RunData> run = new ArrayList<RunData>();

    public RunDataList(List<RunData> run) {

        this.run = run;
    }

    //TODO: implement methods analysing a series of runInstances such as median of the series or minimal linear factor of the approx. solution

    //small helpers
    private double getAverage(Function<RunData, Double> method) {
        if (this.size() == 0) {
            return 0;
        }
        double sum = this.run.stream().mapToDouble(method::apply).sum();
        return sum / this.size();

    }

    protected double getRunningTimeFactor(Function<Double, Double> method) {
        RunData worst = this.getWorstCase();
        return worst.getRunningTimeFactor(method);

    }

    protected double getRunningTimeFactor(Function<Double, Double> method, RunData instanceData) {
        return instanceData.getRunningTimeFactor(method);

    }

    //end small helpers

    public double getAverageTraceLength() {
        return getAverage(RunData::getTraceLength);
    }

    public double getAverageSolutionRatio() {
        return getAverage(RunData::getLinearRunningTimeFactor);

    }

    public double getAverageHintRequest() {
        return getAverage((RunData data) -> (double) data.getHintRequests());
    }

    public void sortBy(Comparator<RunData> method) {
        this.run.sort(method);
    }

    public void sortByWorstCase() {
        sortBy(RunData::compareByRunningTime);

    }

    public void sortByBestCase() {
        sortByWorstCase();
        Collections.reverse(this.run);
    }

    public RunData getWorstCase() {

        return this.run.stream().max(RunData::compareByRunningTime).orElse(null);

    }

    public RunData getBestCase() {
        return this.run.stream().min(RunData::compareByRunningTime).orElse(null);
    }

    public RunDataList filterByOptSol(double toleranceValue) {
        List<RunData> filtered = this.run.stream()
                .filter(instanceData -> instanceData.getOptSolution() < toleranceValue)
                .collect(Collectors.toList());
        return new RunDataList(filtered);
    }

    @Override
    public int compareTo(RunDataList runDataList) {
        return 0;
    }

    //@Override add implements SortableList
    public void sort() {
        sortBy(RunData::compareByRunningTime);
    }

    public int compareBy(Function<RunDataList, Double> method, RunDataList overview) {
        return (int) Math.signum(method.apply(this) - method.apply(overview));
    }

    public int compareByAvgTraceLength(RunDataList overview) {
        return compareBy(RunDataList::getAverageTraceLength, overview);
    }

    public int compareByAvgSolutionQuotient(RunDataList overview) {
        return compareBy(RunDataList::getAverageSolutionRatio, overview);
    }

    public int compareByAvgHintRequests(RunDataList overview) {
        return compareBy(RunDataList::getAverageHintRequest, overview);
    }

}