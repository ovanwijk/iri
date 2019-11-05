package com.iota.iri.service.pathfinding;

import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.model.Hash;
import com.iota.iri.service.pathfinding.impl.PathRef;

import java.util.HashMap;

public interface PathfindingExitCondition {

    boolean isFinished(TransactionViewModel[] endpoints, HashMap<Hash, PathRef> tangleView);
}
