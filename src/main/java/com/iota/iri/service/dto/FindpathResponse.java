package com.iota.iri.service.dto;

import com.iota.iri.model.Hash;
import com.iota.iri.utils.Pair;

import java.util.List;
import java.util.stream.Collectors;

public class FindpathResponse extends AbstractResponse {


    private List<String> txIDs;
    private List<Integer[]> branches;
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
