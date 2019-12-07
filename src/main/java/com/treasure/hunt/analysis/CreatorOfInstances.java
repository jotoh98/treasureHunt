package com.treasure.hunt.analysis;

import com.treasure.hunt.game.GameEngine;
import com.treasure.hunt.game.Move;
import com.treasure.hunt.strategy.hider.Hider;
import com.treasure.hunt.strategy.hider.impl.RandomAngleHintHider;
import com.treasure.hunt.strategy.searcher.Movement;
import com.treasure.hunt.strategy.searcher.Searcher;
import org.locationtech.jts.geom.Point;

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
        return runInstance;
    }
    //TODO look into class Analyzer
    public RunInstanceData createRunInstanceData(List<Move> runInstance){
        return Analyzer.create_runInstanceData(runInstance);
    }

    public RunInstancesOverview createRunInstancesOverview(Point startPos, Point treasurePos,Searcher searcher, RandomAngleHintHider hider, int numberofInstances){

        configure(startPos,treasurePos,searcher,hider);
        RunInstancesOverview overview=new RunInstancesOverview(new ArrayList<RunInstanceData>());

        for (int i = 0; i < numberofInstances; i++) {
            overview.add(createRunInstanceData(createRunInstance()));
        }

        return overview;
    }

}
