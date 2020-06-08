package com.iota.iri.service.tipselection.impl;

import com.google.common.annotations.VisibleForTesting;
import com.iota.iri.conf.TipSelConfig;
import com.iota.iri.controllers.TipsViewModel;
import com.iota.iri.model.Hash;
import com.iota.iri.service.ledger.LedgerService;
import com.iota.iri.service.snapshot.SnapshotProvider;
import com.iota.iri.service.tipselection.*;
import com.iota.iri.storage.Tangle;

import java.security.InvalidAlgorithmParameterException;
import java.util.*;

public class RandomTipSelectorImpl implements TipSelector {

    private final TipsViewModel tipsViewModel;

    public RandomTipSelectorImpl(TipsViewModel tipsViewModel) {
        this.tipsViewModel = tipsViewModel;
    }

    public List<Hash> getTransactionsToApprove(int depth, Optional<Hash> reference) throws Exception {
        try {
            ArrayList<Hash> tips = new ArrayList<>();
            tips.add(tipsViewModel.getRandomNonSolidTipHash());
            for(int i = 0; i < 5; i++){
                Hash t = tipsViewModel.getRandomNonSolidTipHash();
                if(t != tips.get(0)){
                    tips.add(t);
                    break;
                }
            }
            if(tips.size() == 1){
                tips.add(tipsViewModel.getRandomNonSolidTipHash());
            }
            return tips;
        } finally {

        }
    }

}