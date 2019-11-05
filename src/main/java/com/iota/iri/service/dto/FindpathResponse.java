package com.iota.iri.service.dto;

import com.iota.iri.model.Hash;
import com.iota.iri.utils.Pair;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Response for the pathfinding functionality
 */
public class FindpathResponse extends AbstractResponse {


    private List<String> txIDs;
    /**
     * List of branch indexes with integer references connecting 2 transactions
     */
    private List<Integer[]> branches;
    /**
     * List of trunk indexes with integer references connecting 2 transactions
     */
    private List<Integer[]> trunks;
    public static FindpathResponse create(List<Hash> txIDs, List<Integer[]> branches, List<Integer[]> trunks) {
        FindpathResponse res = new FindpathResponse();
        res.txIDs = txIDs.stream().map(t -> t.toString()).collect(Collectors.toList());
        res.branches = branches;
        res.trunks = trunks;
        return res;
    }

    public List<String> getTxIDs(){
        return txIDs;
    }
    public List<Integer[]> getBranches(){
        return branches;
    }
    public List<Integer[]> getTrunks(){
        return trunks;
    }
}
