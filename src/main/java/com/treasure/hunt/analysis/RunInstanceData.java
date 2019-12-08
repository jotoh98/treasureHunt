package com.treasure.hunt.analysis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.treasure.hunt.game.Move;
import com.treasure.hunt.strategy.geom.GeometryItem;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.server.UID;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Getter
public class RunInstanceData implements Comparable<RunInstanceData> {

    private final List<Move> actualRun;

    RunInstanceData(List<Move> actualRun) {
        this.actualRun = actualRun;
    }

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

        for (Move move : actualRun
        ) {
            for (GeometryItem<Point> point : move.getMovement().getPoints()
            ) {
                stepPoints.add(point.getObject());
            }
        }
        return stepPoints;
    }

    public Point getStartPoint() {
        return actualRun.get(0).getMovement().getPoints().get(0).getObject();
    }

    public Point getTreasureLocation() {
        return actualRun.get(0).getTreasureLocation();
    }

    public double getOptSolution() {
        return getStartPoint().distance(getTreasureLocation());
    }

    public double getRunningTimeFactor(Function<Double, Double> method) {
        return getTraceLength() / method.apply(getOptSolution());
    }

    public double getLinearRunningTimeFactor() {
        return getRunningTimeFactor((Double opt) -> opt);
    }

    public double getQuadraticRunningTimeFactor() {
        return this.getRunningTimeFactor((Double opt) -> opt * opt);
    }

    public int getHintRequests() {
        return actualRun.size();
    }

    //TODO finish this in detail with the possibility to dump this into a json file

    public String toJsonString() throws JsonProcessingException {
        String json = new ObjectMapper().writeValueAsString(this);
        return json;
    }

    public void saveRunInstance() throws IOException {
        saveRunInstance("C:\\Users\\Shlohmo\\Documents\\treasureHunt\\src\\main\\java\\com\\treasure\\hunt\\analysis\\RunInstanceSaveFolder");
    }

    public void saveRunInstance(String path) {

        String hash = ((Integer) System.identityHashCode(this)).toString();
        String filepath = path + "\\RunInstanceSaveFolder\\" + hash + ".txt";

        try {
            Path file = Paths.get(filepath);
            Files.write(file, Collections.singletonList(this.toJsonString()), StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error("Could not create or write file, Dorians Schuld", e);
        }
    }

    @Override
    public int compareTo(RunInstanceData instanceData) {
        return (int) (getLinearRunningTimeFactor()-instanceData.getLinearRunningTimeFactor());
    }

    public int comparebyHints(RunInstanceData runInstance){
        return getHintRequests()-runInstance.getHintRequests();
    }

    public int comparebyOptSol(RunInstanceData runInstance){
        return (int) (getOptSolution()-runInstance.getOptSolution());
    }




}

