package com.iota.iri.service.pathfinding;

import com.iota.iri.model.Hash;

import java.util.LinkedHashSet;
import java.util.List;

public interface SubtangleDescription {


    List<Hash> transactionHashes();
    List<Integer[]> branchVertices();
    List<Integer[]> trunkVertices();


}
