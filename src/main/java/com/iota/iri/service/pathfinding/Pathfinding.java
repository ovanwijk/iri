package com.iota.iri.service.pathfinding;

import com.iota.iri.model.Hash;
import com.iota.iri.storage.Tangle;

public interface Pathfinding {


    void init(Tangle tangle);
    SubtangleDescription findPath(Hash start, Hash[] endPoints) throws Exception;





}
