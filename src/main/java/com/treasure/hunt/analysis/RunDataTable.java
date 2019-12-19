package com.treasure.hunt.analysis;

import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
@NoArgsConstructor
public class RunDataTable implements Comparable<RunDataTable> {

    @Delegate
    private List<RunDataList> list = new ArrayList<RunDataList>();

    public RunDataTable(List<RunDataList> list) {
        this.list = list;
    }

    //TODO add compare ,best/worse case bla bla bla
    private double getAverage(Function<RunDataList, Double> method) {
        if (this.size() == 0) {
            return 0;
        }
        double sum = list.stream().mapToDouble(method::apply).sum();
        return sum / this.size();

    }

    //end small helpers

    public double getAverageTraceLength() {
        return getAverage(RunDataList::getAverageTraceLength);
    }

    public double getAverageSolutionQuotient() {
        return getAverage(RunDataList::getAverageSolutionRatio);

    }

    public double getAverageHintRequest() {
        return getAverage((RunDataList data) -> data.getAverageHintRequest());
    }

    public void sortBy(Comparator<RunDataList> method) {
        this.list.sort(method);
    }

    public void sortByWorstCase() {
        sortBy(RunDataList::compareByAvgSolutionQuotient);

    }

    public void sortByBestCase() {
        sortByWorstCase();
        Collections.reverse(this.list);
    }

    public RunDataList getWorstCase() {

        return this.list.stream().max(RunDataList::compareByAvgSolutionQuotient).orElse(null);

    }

    public RunDataList getBestCase() {
        return this.list.stream().min(RunDataList::compareByAvgSolutionQuotient).orElse(null);
    }

    public RunDataTable filterByOptSol(double toleranceValue) {

        List<RunDataList> filtered = this.list.stream()
                .map(instancesOverview -> instancesOverview.filterByOptSol(toleranceValue))
                .collect(Collectors.toList());

        return new RunDataTable(filtered);
    }

    //@Override add implements SortableList
    public void sort() {
        sortBy(RunDataList::compareByAvgSolutionQuotient);
    }

    public int compareBy(Function<RunDataTable, Double> method, RunDataTable overview) {
        return (int) Math.signum(method.apply(this) - method.apply(overview));
    }

    public int compareByAvgTraceLength(RunDataTable overview) {
        return compareBy(RunDataTable::getAverageTraceLength, overview);
    }

    public int compareByAvgSolutionQuotient(RunDataTable overview) {
        return compareBy(RunDataTable::getAverageSolutionQuotient, overview);
    }

    public int compareByAvgHintRequests(RunDataTable overview) {
        return compareBy(RunDataTable::getAverageHintRequest, overview);
    }

    @Override
    public int compareTo(RunDataTable runDataTable) {
        return 0;
    }

    public List<Double> getRunTimeList(){
        List<Double> runtimelist=new ArrayList<Double>();
        list.forEach(list->runtimelist.add(list.getAverageSolutionRatio()));
        return runtimelist;
    }
}