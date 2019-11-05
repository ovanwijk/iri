package com.iota.iri.service.pathfinding;

import com.iota.iri.model.Hash;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Abstract description of a subtangle relations
 */
public interface SubtangleDescription {

    /**
     * List of transaction IDs, the order of this is important and the indexes are used to
     * determine the vertices
     */
    List<Hash> transactionHashes();

    /**
     * A list of an integer pair, the first integer describing the index of the -from- transaction and the
     * second integer the -to- transactions, for branches only. These integers are indexes related to the transactionHashs()
     * @return
     */
    List<Integer[]> branchVertices();

    /**
     * A list of an integer pair, the first integer describing the index of the -from- transaction and the
     * second integer the -to- transactions, for trunks only. These integers are indexes related to the transactionHashs()
     * @return
     */
    List<Integer[]> trunkVertices();


}
