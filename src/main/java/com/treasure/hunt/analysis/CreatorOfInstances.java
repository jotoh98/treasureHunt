package com.treasure.hunt.analysis;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.Move;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hider.impl.RandomAngleHintHider;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import com.treasure.hunt.strategy.searcher.impl.NaiveAngleSearcher;
import com.treasure.hunt.utils.JTSUtils;
import org.locationtech.jts.geom.Point;
import com.treasure.hunt.utils.JTSUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

//TODO: change Hider Interface in order to set a none random treasureLocation, GameEngine could as well be optimize for analytics but this works as well

public class CreatorOfInstances {
    protected GameEngine engine;

    //TODO RandomAngleHintHider has to be changed to Interface Hider
    public void configure(Point startPos, Point treasurePos,Searcher searcher, RandomAngleHintHider hider){
        hider.setTreasurePos(treasurePos);
        engine=new GameEngine(searcher,hider);
        engine.setSearcherPos(startPos);
    }

    public List<Move> createRunInstance(){
        List<Move> runInstance=new ArrayList<Move>();

        runInstance.add(engine.init());
        while(!engine.isFinished()){
            runInstance.add(engine.move());
        }
        //to reset the gameengine
        engine.setFinished(false);
        engine.setFirstMove(true);
        return runInstance;
    }


    public RunInstanceData createRunInstanceData(List<Move> runInstance){
        return new RunInstanceData(runInstance);
    }

    public RunInstancesOverview createRunInstancesOverview(Point startPos, Point treasurePos, Class<NaiveAngleSearcher> searcher, Class<RandomAngleHintHider> hider, int nrOfInstances) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        configure(startPos,treasurePos,searcher.getDeclaredConstructor().newInstance(),hider.getDeclaredConstructor().newInstance());
        RunInstancesOverview overview=new RunInstancesOverview(new ArrayList<RunInstanceData>());

        for (int i = 0; i < nrOfInstances; i++) {
            overview.add(createRunInstanceData(createRunInstance()));

        }

        return overview;
    }

    public void test() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException{
        RunInstancesOverview testoverview= createRunInstancesOverview(JTSUtils.createPoint(0,0),JTSUtils.createPoint(90,90), NaiveAngleSearcher.class,RandomAngleHintHider.class,100);
        System.out.println("AvgTarceLength: "+testoverview.getAverageTraceLength());
        System.out.println("AvgHIntRequests: "+testoverview.getAverageHintRequest());
        System.out.println("WorstCaseLinearFactor: "+testoverview.getWorstCase().getLinearRunningTimeFactor());
        System.out.println("Size: "+testoverview.size());
        testoverview.get(2).printRun();
    }

}
