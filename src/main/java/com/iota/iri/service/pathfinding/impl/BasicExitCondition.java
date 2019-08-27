package com.iota.iri.service.pathfinding.impl;

import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.model.Hash;
import com.iota.iri.service.pathfinding.PathfindingExitCondition;

import java.util.HashMap;

public class BasicExitCondition implements PathfindingExitCondition {
    @Override
    public boolean isFinished(TransactionViewModel[] endpoints, HashMap<Hash, PathRef> tangleView) {
        for(TransactionViewModel endpoint: endpoints){
            if(!tangleView.containsKey(endpoint.getHash())){
               return false;
            }
        }
        return true;
    }
}
