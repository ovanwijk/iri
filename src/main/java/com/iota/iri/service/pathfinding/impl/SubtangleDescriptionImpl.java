package com.iota.iri.service.pathfinding.impl;

import com.iota.iri.model.Hash;
import com.iota.iri.service.pathfinding.SubtangleDescription;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

public class SubtangleDescriptionImpl implements SubtangleDescription {


    public List<Hash> transactions = new LinkedList<>();
    public List<Integer[]> branches = new LinkedList<>();
    public List<Integer[]> trunks = new LinkedList<>();



    @Override
    public List<Hash> transactionHashes() {
        return transactions;
    }

    @Override
    public List<Integer[]> branchVertices() {
        return branches;
    }

    @Override
    public List<Integer[]> trunkVertices() {
        return trunks;
    }

}
