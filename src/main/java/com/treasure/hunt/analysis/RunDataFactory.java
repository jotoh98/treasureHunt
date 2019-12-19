package com.treasure.hunt.analysis;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.Move;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hider.impl.RandomAngleHintHider;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.strategy.searcher.impl.NaiveAngleSearcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Point;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

//TODO: change Hider Interface in order to set a none random treasureLocation, GameEngine could as well be optimize for analytics but this works as well

public class RunDataFactory {
    int maxHint=0;

    public List<Move> createRunInstance(Hider hider, Searcher searcher, Point treasurePos, Point startPos) {

        List<Move> runInstance = new ArrayList<Move>();
        hider.setTreasurePos(treasurePos);
        GameEngine engine = new GameEngine(searcher, hider);
        engine.setSearcherPos(startPos);
        engine.init();

        if(maxHint > 0){

            for (int i = 0; i < maxHint; i++) {
                runInstance.add(engine.move());
                if (engine.isFinished()){break;}
            }

        }else {

            while (!engine.isFinished()) {
                runInstance.add(engine.move());
            }

        }
        return runInstance;
    }


    public RunData createRunData(List<Move> runInstance) {
        return new RunData(runInstance);
    }

    public RunDataList createRunDataList(Point startPos, Point treasurePos, Class<? extends Hider> hider, Class<? extends Searcher> searcher, int nrOfInstances)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        RunDataList overview = new RunDataList(new ArrayList<RunData>());

        for (int i = 0; i < nrOfInstances; i++) {
            overview.add(createRunData(createRunInstance(hider.getDeclaredConstructor().newInstance(), searcher.getDeclaredConstructor().newInstance(), treasurePos, startPos)));
        }

        return overview;
    }

    public void test() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        RunDataList testoverview = createRunDataList(JTSUtils.createPoint(0, 0), JTSUtils.createPoint(90, 90), RandomAngleHintHider.class, NaiveAngleSearcher.class, 100);
        System.out.println("AvgTarceLength: " + testoverview.getAverageTraceLength());
        System.out.println("AvgHintRequests: " + testoverview.getAverageHintRequest());
        System.out.println("WorstCaseLinearFactor: " + testoverview.getWorstCase().getLinearRunningTimeFactor());
        System.out.println("Size: " + testoverview.size());
        testoverview.get(2).printRun();
    }


    @SafeVarargs
    public final RunDataTable createRunDataTableOnSearchers(Point startPos, Point treasurePos, Class<? extends Hider> hider, int nrOfInstances, Class<? extends Searcher>... searchers) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        RunDataTable runDataTable = new RunDataTable();
        for (int i = 0; i < searchers.length; i++) {

            runDataTable.add(createRunDataList(startPos, treasurePos, hider, searchers[i], nrOfInstances));
        }
        return runDataTable;
    }

    @SafeVarargs
    public final RunDataTable createRunDataTableOnHiders(Point startPos, Point treasurePos, Class<? extends Searcher> searcher, int nrOfInstances, Class<? extends Hider>... hiders)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        RunDataTable runDataTable = new RunDataTable();
        for (int i = 0; i < hiders.length; i++) {
            runDataTable.add(createRunDataList(startPos, treasurePos, hiders[i], searcher, nrOfInstances));
        }
        return runDataTable;
    }

    public final RunDataFactory maxHintFactory(int maxHint){
        this.maxHint=maxHint;
        return this;
    }

    public final RunDataTable createRunDataTableOnDistance(Point startPos, Class<? extends Searcher> searcher,Class<? extends Hider> hider,int nrOfInstances,double growthLimit, double stepsize) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        RunDataTable table=new RunDataTable();
        Point treasure=startPos;

        for (double i = 0; i <growthLimit ; i+=stepsize) {
            treasure=JTSUtils.createPoint(treasure.getX(),treasure.getY()+i);
            table.add(createRunDataList(startPos,treasure,hider,searcher,nrOfInstances));
        }
        return table;
    }


}
