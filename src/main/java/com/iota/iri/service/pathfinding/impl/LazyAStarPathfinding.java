package com.iota.iri.service.pathfinding.impl;

import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.model.Hash;
import com.iota.iri.service.pathfinding.Pathfinding;
import com.iota.iri.service.pathfinding.SubtangleDescription;
import com.iota.iri.storage.Tangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Lazy A Star pathfinding algoritm.
 */
public class LazyAStarPathfinding implements Pathfinding {
    private static final Logger log = LoggerFactory.getLogger(LazyAStarPathfinding.class);
    Tangle tangle;


    public LazyAStarPathfinding(Tangle tangle){
        this.tangle = tangle;
    }

    @Override
    public void init(Tangle tangle){
        this.tangle = tangle;
    }
    @Override
    public SubtangleDescription findPath(Hash startHash, Hash[] endpointsHashes) throws Exception {
        long ts = System.currentTimeMillis();
        HashMap<Hash, PathRef> tangleView = new HashMap<>();
        TransactionViewModel start = TransactionViewModel.find(tangle, startHash.bytes());

        if(start.getTransaction().bytes() == null){
            throw new Exception("Cannot find start transaction: " + startHash.toString());
        }

        TreeMap<Long, TransactionViewModel> endpoints = new TreeMap<>(Collections.reverseOrder()); //Reverse order to make the 'newest' first

        for(int i = 0; i < endpointsHashes.length; i++){
            TransactionViewModel found = TransactionViewModel.find(tangle, endpointsHashes[i].bytes());

            if(found.getTransaction().bytes() == null){
                throw new Exception("Cannot find destination transaction: " + endpointsHashes[i].toString());
            }
            found.setMetadata();
            long sortIncrease = 0;
            while (endpoints.containsKey(found.getTimestamp() + sortIncrease)){
                sortIncrease += 1;
            }

            endpoints.put(found.getTimestamp() + sortIncrease, found);


        }

        //TODO timestamp sanity check, transactions cannot be older than the tangle! Removes false positives.


        start.setMetadata(); //required to read the timestamp.
        tangleView.put(start.getHash(), new PathRef(startHash,null, 's', start.getBranchTransactionHash(), start.getTrunkTransactionHash(), 0));

        SortedMap<Long, List<ApproveeStep>> callQueue = new TreeMap<>(); //Ordered map, oldest transactions first.
        SortedMap<Long, List<ApproveeStep>> overReachQueue = new TreeMap<>(); //Transactions that reach beyond the timestamp
        overReachQueue.put(start.getTimestamp(), new ArrayList<ApproveeStep>(){
                    {
                        add(new ApproveeStep(start, null, 0));
                    }
                });

        SubtangleDescriptionImpl result = new SubtangleDescriptionImpl();
        HashMap<Hash, Integer> resultIndex = new LinkedHashMap<>();
        Set<String> edgeIndex = new HashSet<>();
        Integer indexCounter = 0;
        for(TransactionViewModel endpoint: endpoints.values()){
            callQueue.putAll(overReachQueue);
            overReachQueue = new TreeMap<>();
            exploreTangle(callQueue, overReachQueue, endpoint, tangleView);

            //ArrayList<String> path = new ArrayList<>();
            PathRef currentRef = tangleView.get(endpoint.getHash());

            do{
                if(!resultIndex.containsKey(currentRef.txID)){
                    result.transactions.add(currentRef.txID);
                    resultIndex.put(currentRef.txID, indexCounter);
                    indexCounter += 1;
                }

                if(!resultIndex.containsKey(currentRef.shortestPath)){
                    result.transactions.add(currentRef.shortestPath);
                    resultIndex.put(currentRef.shortestPath, indexCounter);
                    indexCounter += 1;
                }
                if(currentRef.branchOrTrunk != 's' && !edgeIndex.contains(currentRef.txID.toString() + currentRef.shortestPath.toString() )){
                    List<Integer[]> branchOrTrunk = (currentRef.branchOrTrunk == 'b' ? result.branches : result.trunks);
                    branchOrTrunk.add(new Integer[]{resultIndex.get(currentRef.shortestPath), resultIndex.get(currentRef.txID)});
                    edgeIndex.add(currentRef.txID.toString() + currentRef.shortestPath.toString() );
                }
                //  path.add(currentRef.shortestPath);
                currentRef = tangleView.get(currentRef.shortestPath);
            }while(currentRef.step != 0);
            log.trace("Breakpoint");

        }
        log.info("Pathfinding took: " + (System.currentTimeMillis() - ts) + "ms");
        return result;
    }


    private int recursiveWeightUpdate(Hash hash, Hash approvee, int diff, int refCounter, HashMap<Hash, PathRef> tangleView){
        if(hash != null) {
            PathRef p = tangleView.get(hash);
            if (p != null && p.shortestPath == approvee) {
                p.step -= diff;
                refCounter += recursiveWeightUpdate(p.branch, hash, diff, refCounter, tangleView);
                refCounter += recursiveWeightUpdate(p.trunk, hash, diff, refCounter, tangleView);
            }
        }
        return refCounter;

    }


    private void exploreTangle(SortedMap<Long, List<ApproveeStep>> callQueue, SortedMap<Long, List<ApproveeStep>> overReach, TransactionViewModel endpoint, HashMap<Hash, PathRef> tangleView) throws Exception {
        long minimumTimestamp = endpoint.getTimestamp();
        long txCount = 0;
        long overReachCount = 0;
//        try {
            do {
                List<ApproveeStep> transactions = callQueue.remove(callQueue.firstKey());
                for (ApproveeStep st : transactions) {
                    txCount += 1;
                    TransactionViewModel tvm = st.tvm;
                    int currentStep = st.step; //how many steps deep it is currently

                    //If the current transactions over reach the timestamp boundry add them to the overReach queue
                    //This queue will be used to start for the next transaction.

                    //For the sake of pathfinding it doesnt matter if we walk the trunk or the branch but for the results it does
                    TransactionViewModel[] branchAndTrunk = new TransactionViewModel[]{tvm.getBranchTransaction(tangle), tvm.getTrunkTransaction(tangle)};

                    for (int i = 0; i < 2; i++) {
                        TransactionViewModel branchOrTrunk = branchAndTrunk[i];
                        branchOrTrunk.setMetadata();
                        if (branchOrTrunk.getTimestamp() > 0) {
                            SortedMap<Long, List<ApproveeStep>> queueReference =
                                    (branchOrTrunk.getTimestamp() > (minimumTimestamp-600) ? callQueue : overReach);

                            if (!tangleView.containsKey(branchOrTrunk.getHash())) {
                                tangleView.put(branchOrTrunk.getHash(), new PathRef(branchOrTrunk.getHash(),
                                        tvm.getHash(),
                                        i == 0 ? 'b' : 't', //here we use the index to put back the trunk or branch information
                                        branchOrTrunk.getBranchTransactionHash(), branchOrTrunk.getTrunkTransactionHash(), currentStep + 1));
                                //calculate the timestamp based difference between transactions
                                long timewarpDistance = tvm.getTimestamp() - branchOrTrunk.getTimestamp();
                                //assume the same distance is travelled on the same path(either branch or trunk).
                                long projectedCallqueue = branchOrTrunk.getTimestamp() - timewarpDistance;
                                if (queueReference.containsKey(projectedCallqueue)) {
                                    queueReference.get(projectedCallqueue).add(new ApproveeStep(branchOrTrunk, tvm.getHash(), currentStep + 1));//When it already exists we just append to the existing list.
                                } else {
                                    queueReference.put(projectedCallqueue, new ArrayList<ApproveeStep>() {
                                        {
                                            add(new ApproveeStep(branchOrTrunk, tvm.getHash(), currentStep + 1));
                                        }
                                    });
                                }
                            } else {
                                //Already contains a ref.
                                PathRef p = tangleView.get(branchOrTrunk.getHash());
                                if (currentStep < p.step) {
                                    //Found a faster path up
                                    p.shortestPath = tvm.getHash();
                                    p.branchOrTrunk = i == 0 ? 'b' : 't';
                                    //TODO verify if this is even required
//                                    int updates = recursiveWeightUpdate(branchOrTrunk.getHash(), tvm.getHash(), p.step - currentStep, 0, tangleView);
//                                    if(updates > 0) {
//                                        System.out.println("Updates: " + updates);
//                                    }
                                }
                            }
                        }else{
                            overReachCount += 1;
                        }
                    }
                }

            } while (!callQueue.isEmpty() && !tangleView.containsKey(endpoint.getHash()));

            if (callQueue.isEmpty()) {
                throw new Exception("Paths not found for endpoint: " +  endpoint.getHash() + " txCount " + txCount + " size: " +overReach.size() + " No path:" + overReachCount);
            }

    }
}
