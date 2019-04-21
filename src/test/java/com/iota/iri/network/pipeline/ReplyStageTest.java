package com.iota.iri.network.pipeline;

import com.iota.iri.TangleMockUtils;
import com.iota.iri.TransactionValidator;
import com.iota.iri.conf.NodeConfig;
import com.iota.iri.controllers.TipsViewModel;
import com.iota.iri.model.Hash;
import com.iota.iri.network.FIFOCache;
import com.iota.iri.network.NeighborRouter;
import com.iota.iri.network.SampleTransaction;
import com.iota.iri.network.TransactionRequester;
import com.iota.iri.network.neighbor.impl.NeighborImpl;
import com.iota.iri.network.neighbor.impl.NeighborMetricsImpl;
import com.iota.iri.service.milestone.LatestMilestoneTracker;
import com.iota.iri.storage.Tangle;

import java.security.SecureRandom;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.fail;

public class ReplyStageTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Tangle tangle;

    @Mock
    private TransactionValidator transactionValidator;

    @Mock
    private NeighborRouter neighborRouter;

    @Mock
    private NodeConfig nodeConfig;

    @Mock
    private TipsViewModel tipsViewModel;

    @Mock
    private LatestMilestoneTracker latestMilestoneTracker;

    @Mock
    private FIFOCache<Long, Hash> recentlySeenBytesCache;

    @Mock
    private TransactionRequester transactionRequester;

    @Mock
    private NeighborImpl neighbor;

    @Mock
    private NeighborMetricsImpl neighborMetrics;

    @Mock
    private SecureRandom random;

    @Test
    public void usingTheNullHashARandomTipIsGettingReplied() {
        Mockito.when(random.nextDouble()).thenReturn(0.4, 0.6d);
        Mockito.when(nodeConfig.getpSendMilestone()).thenReturn(0.5);
        Mockito.when(nodeConfig.getpReplyRandomTip()).thenReturn(0.5);
        Mockito.when(neighbor.getMetrics()).thenReturn(neighborMetrics);
        Mockito.when(transactionRequester.numberOfTransactionsToRequest()).thenReturn(1);
        Mockito.when(tipsViewModel.getRandomSolidTipHash()).thenReturn(SampleTransaction.CURL_HASH_OF_SAMPLE_TX);
        TangleMockUtils.mockTransaction(tangle, SampleTransaction.CURL_HASH_OF_SAMPLE_TX,
                SampleTransaction.SAMPLE_TRANSACTION);

        ReplyStage stage = new ReplyStage(neighborRouter, nodeConfig, tangle, tipsViewModel, latestMilestoneTracker,
                recentlySeenBytesCache, transactionRequester, random);
        ReplyPayload replyPayload = new ReplyPayload(neighbor, Hash.NULL_HASH);
        ProcessingContext ctx = new ProcessingContext(replyPayload);
        stage.process(ctx);

        try {
            Mockito.verify(neighborRouter).gossipTransactionTo(Mockito.any(), Mockito.any());
            Mockito.verify(recentlySeenBytesCache).put(SampleTransaction.BYTES_DIGEST_OF_SAMPLE_TX,
                    SampleTransaction.CURL_HASH_OF_SAMPLE_TX);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void usingASuppliedRequestedHashTheTransactionIsReplied() {
        TangleMockUtils.mockTransaction(tangle, SampleTransaction.CURL_HASH_OF_SAMPLE_TX,
                SampleTransaction.SAMPLE_TRANSACTION);

        ReplyStage stage = new ReplyStage(neighborRouter, nodeConfig, tangle, tipsViewModel, latestMilestoneTracker,
                recentlySeenBytesCache, transactionRequester, random);
        ReplyPayload replyPayload = new ReplyPayload(neighbor, SampleTransaction.CURL_HASH_OF_SAMPLE_TX);
        ProcessingContext ctx = new ProcessingContext(replyPayload);
        stage.process(ctx);

        try {
            Mockito.verify(random, Mockito.never()).nextDouble();
            Mockito.verify(neighborRouter).gossipTransactionTo(Mockito.any(), Mockito.any());
            Mockito.verify(recentlySeenBytesCache).put(SampleTransaction.BYTES_DIGEST_OF_SAMPLE_TX,
                    SampleTransaction.CURL_HASH_OF_SAMPLE_TX);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void usingASuppliedRequestedHashButNotHavingItWillPutItInTheRequestQueue() {
        Mockito.when(random.nextDouble()).thenReturn(0.4);
        Mockito.when(nodeConfig.getpPropagateRequest()).thenReturn(0.5);

        TangleMockUtils.mockTransaction(tangle, SampleTransaction.CURL_HASH_OF_SAMPLE_TX, null);

        ReplyStage stage = new ReplyStage(neighborRouter, nodeConfig, tangle, tipsViewModel, latestMilestoneTracker,
                recentlySeenBytesCache, transactionRequester, random);
        ReplyPayload replyPayload = new ReplyPayload(neighbor, SampleTransaction.CURL_HASH_OF_SAMPLE_TX);
        ProcessingContext ctx = new ProcessingContext(replyPayload);
        stage.process(ctx);

        try {
            Mockito.verify(transactionRequester).requestTransaction(SampleTransaction.CURL_HASH_OF_SAMPLE_TX, false);
            Mockito.verify(neighborRouter, Mockito.never()).gossipTransactionTo(Mockito.any(), Mockito.any());
            Mockito.verify(recentlySeenBytesCache, Mockito.never()).put(SampleTransaction.BYTES_DIGEST_OF_SAMPLE_TX,
                    SampleTransaction.CURL_HASH_OF_SAMPLE_TX);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}