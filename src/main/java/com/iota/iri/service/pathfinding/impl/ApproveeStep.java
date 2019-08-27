package com.iota.iri.service.pathfinding.impl;

import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.model.Hash;
import com.iota.iri.model.persistables.Approvee;

public class ApproveeStep {
    public final TransactionViewModel tvm;
    public final Hash approveeHash;
    public final int step;

    public ApproveeStep(TransactionViewModel tvm, Hash approveeHash, int step){
        this.tvm = tvm;
        this.approveeHash = approveeHash;
        this.step = step;
    }
}
