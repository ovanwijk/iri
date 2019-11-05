package com.iota.iri.service.pathfinding.impl;


import com.iota.iri.model.Hash;

public class PathRef {
    public Hash shortestPath;
    public Hash txID;
    public char branchOrTrunk = 'b';
    public int step = 0;
    public final Hash branch;
    public final Hash trunk;


    public PathRef(Hash txID, Hash shortestPath, char branchOrTrunk, Hash branch, Hash trunk, int step){
        this.txID = txID;
        this.shortestPath = shortestPath;
        this.branch = branch;
        this.trunk = trunk;
        this.step = step;
        this.branchOrTrunk = branchOrTrunk;
    }
}
