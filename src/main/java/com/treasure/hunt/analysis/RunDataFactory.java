package com.treasure.hunt.analysis;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.Move;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hider.impl.RandomAngleHintHider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.strategy.searcher.impl.NaiveAngleSearcher;
import com.treasure.hunt.utils.JTSUtils;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

//TODO: change Hider Interface in order to set a none random treasureLocation, GameEngine could as well be optimize for analytics but this works as well
@Slf4j
public class RunDataFactory {
    int maxHint=0;

    public List<Move> createRunInstance(Hider hider, Searcher searcher, double distance, Point startPos) {

        List<Move> runInstance = new ArrayList<Move>();
        hider.setTreasureDistance(distance);
        GameEngine engine = new GameEngine(searcher, hider);
        engine.setSearcherPos(startPos);
        engine.init();

        if(maxHint > 0){

            for (int i = 0; i < maxHint; i++) {
                runInstance.add(engine.move());
                if (engine.getFinished().get()){break;}
            }

        }else {

            while (!engine.getFinished().get()) {
                runInstance.add(engine.move());
            }

        }
        return runInstance;
    }


    public RunData createRunData(List<Move> runInstance) {
        return new RunData(runInstance);
    }

    public RunDataList createRunDataList(Point startPos, double distance, Class<? extends Hider> hider, Class<? extends Searcher> searcher, int nrOfInstances)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        RunDataList overview = new RunDataList(new ArrayList<>());

        for (int i = 0; i < nrOfInstances; i++) {
            overview.add(createRunData(createRunInstance(hider.getDeclaredConstructor().newInstance(), searcher.getDeclaredConstructor().newInstance(), distance, startPos)));
        }

        return overview;
    }

    public void test() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        RunDataList testoverview = createRunDataList(JTSUtils.createPoint(0, 0), 100, RandomAngleHintHider.class, NaiveAngleSearcher.class, 100);

        System.out.println("AvgTarceLength: " + testoverview.getAverageTraceLength());
        System.out.println("AvgHintRequests: " + testoverview.getAverageHintRequest());
        System.out.println("WorstCaseLinearFactor: " + testoverview.getWorstCase().getLinearRunningTimeFactor());
        System.out.println("Size: " + testoverview.size());
        testoverview.get(2).printRun();
    }


    @SafeVarargs
    public final RunDataTable createRunDataTableOnSearchers(Point startPos, double distance, Class<? extends Hider> hider, int nrOfInstances, Class<? extends Searcher>... searchers) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        RunDataTable runDataTable = new RunDataTable();
        for (int i = 0; i < searchers.length; i++) {

            runDataTable.add(createRunDataList(startPos, distance, hider, searchers[i], nrOfInstances));
        }
        return runDataTable;
    }

    @SafeVarargs
    public final RunDataTable createRunDataTableOnHiders(Point startPos, double distance, Class<? extends Searcher> searcher, int nrOfInstances, Class<? extends Hider>... hiders)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        RunDataTable runDataTable = new RunDataTable();
        for (int i = 0; i < hiders.length; i++) {
            runDataTable.add(createRunDataList(startPos, distance, hiders[i], searcher, nrOfInstances));
        }
        return runDataTable;
    }

    public final RunDataFactory maxHintFactory(int maxHint){
        this.maxHint=maxHint;
        return this;
    }

    public final RunDataTable createRunDataTableOnDistance(Point startPos, Class<? extends Searcher> searcher,Class<? extends Hider> hider,int nrOfInstances,double growthLimit, double stepsize) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        RunDataTable table=new RunDataTable();

        for (double radius = 0; radius <growthLimit ; radius+=stepsize) {
            table.add(createRunDataList(startPos,radius,hider,searcher,nrOfInstances));
        }
        return table;
    }

}
