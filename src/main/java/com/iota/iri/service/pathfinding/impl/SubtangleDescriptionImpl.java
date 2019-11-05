package com.iota.iri.service.pathfinding.impl;

import com.iota.iri.model.Hash;
import com.iota.iri.service.pathfinding.SubtangleDescription;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * SubtangleDescription implementation
 */
public class SubtangleDescriptionImpl implements SubtangleDescription {


    List<Hash> transactions = new LinkedList<>();
    List<Integer[]> branches = new LinkedList<>();
    List<Integer[]> trunks = new LinkedList<>();


    /**
     * @see SubtangleDescription#transactionHashes()
     * @return
     */
    @Override
    public List<Hash> transactionHashes() {
        return transactions;
    }
    /**
     * @see SubtangleDescription#branchVertices()
     * @return
     */
    @Override
    public List<Integer[]> branchVertices() {
        return branches;
    }
    /**
     * @see SubtangleDescription#trunkVertices()
     * @return
     */
    @Override
    public List<Integer[]> trunkVertices() {
        return trunks;
    }

}
