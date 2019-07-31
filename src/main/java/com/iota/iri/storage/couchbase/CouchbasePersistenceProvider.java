package com.iota.iri.storage.couchbase;


import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.deps.io.netty.buffer.Unpooled;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketType;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.cluster.CompressionMode;
import com.couchbase.client.java.cluster.EjectionMethod;
import com.couchbase.client.java.document.BinaryDocument;
import com.couchbase.client.java.document.ByteArrayDocument;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.query.Index;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.Statement;
import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.model.Hash;
import com.iota.iri.model.HashFactory;
import com.iota.iri.model.persistables.*;
import com.iota.iri.storage.ExternalPersistenceProvider;
import com.iota.iri.storage.Indexable;
import com.iota.iri.storage.Persistable;
import com.iota.iri.storage.PersistenceProvider;
import com.iota.iri.utils.IotaIOUtils;
import com.iota.iri.utils.Pair;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.*;


public class CouchbasePersistenceProvider extends ExternalPersistenceProvider {

    private static final Logger log = LoggerFactory.getLogger(CouchbasePersistenceProvider.class);
    private static final Pair<Indexable, Persistable> PAIR_OF_NULLS = new Pair<>(null, null);

    private final SecureRandom seed = new SecureRandom();
    private final String txBucketName;
    private final String txMetaDataBucketName;
    private final String[] couchbaseNodes;
    private final String logPath;
    private final String couchbaseUsername;
    private final String couchbasePassword;
    private final boolean couchbaseCheckInitialization;

    private Cluster cluster;
    private Bucket txBucket;
    private Bucket metadataBucket;
    private final Statement bundleStatement;// = select("meta(tx_metadata).id").from(i("tx_metadata")).where(x("bundle").eq("$bundle"));
    private final Statement addressStatement;// = select("meta(tx_metadata).id").from(i("tx_metadata")).where(x("address").eq("address"));
    private final Statement tagStatement;// = select("meta(tx_metadata).id").from(i("tx_metadata")).where(x("tag").eq("tag"));
    private final Statement approveesStatement;// = select("meta(tx_metadata).id").from(i("tx_metadata")).where(x("branch").eq("$hash").or(x("trunk").eq("$hash")));


    private boolean available;

    public CouchbasePersistenceProvider(String[] couchbaseNodes, String couchbaseUsername,
                                        String couchbasePassword,
                                        boolean couchbaseCheckInitialization,
                                        String logPath,
                                        String txBucketName,
                                        String txMetaDataBucketName) {
        this.couchbaseNodes = couchbaseNodes;
        this.couchbaseUsername = couchbaseUsername;
        this.couchbasePassword = couchbasePassword;
        this.txBucketName = txBucketName;
        this.txMetaDataBucketName = txMetaDataBucketName;
        this.logPath = logPath;
        this.couchbaseCheckInitialization = couchbaseCheckInitialization;
        this.bundleStatement = select("meta(t).id").from(i(txMetaDataBucketName).as("t")).where(x("bundle").eq("$bundle"));
        this.addressStatement = select("meta(t).id").from(i(txMetaDataBucketName).as("t")).where(x("address").eq("$address"));
        this.tagStatement = select("meta(t).id").from(i(txMetaDataBucketName).as("t")).where(x("tag").eq("$tag"));
        this.approveesStatement = select("meta(t).id").from(i(txMetaDataBucketName).as("t")).where(x("branch").eq("$hash").or(x("trunk").eq("$hash")));


    }

    @Override
    public void init() throws Exception {
        log.info("Initializing Couchbase on " + couchbaseNodes);
        initDB();
        available = true;
        log.info("Couchbase persistence provider initialized.");
    }

    @Override
    public boolean isAvailable() {
        return this.available;
    }


    @Override
    public void shutdown() {
        txBucket.close();
        metadataBucket.close();
    }

    @Override
    public boolean save(Persistable thing, Indexable index) throws Exception {

        if(!(thing instanceof Transaction)) {
            return false;
        }
        Transaction transaction = ((Transaction) thing);

        saveToCouchbase(transaction, (Hash)index);

        return true;
    }


    @Override
    public boolean exists(Class<?> model, Indexable key) throws Exception {

        return false; //TODO see if this is ok
    }




    @Override
    public Persistable get(Class<?> model, Indexable index) throws Exception {
        Persistable object = (Persistable) model.newInstance();

        if(object instanceof Bundle){
            List<N1qlQueryRow> result = cluster.query(N1qlQuery.parameterized(bundleStatement, JsonObject.create().put("bundle", index.toString()))).allRows();
            Bundle b = (Bundle)object;
            for(N1qlQueryRow aa:result){
                b.set.add(HashFactory.TRANSACTION.create(aa.value().getString("id")));
            }
            return b;
        }

        if(object instanceof Address){
            List<N1qlQueryRow> result = cluster.query(N1qlQuery.parameterized(addressStatement, JsonObject.create().put("address", index.toString()))).allRows();
            Address b = (Address)object;
            for(N1qlQueryRow aa:result){
                b.set.add(HashFactory.TRANSACTION.create(aa.value().getString("id")));
            }
            return b;
        }

        if(object instanceof Tag){
            List<N1qlQueryRow> result = cluster.query(N1qlQuery.parameterized(tagStatement, JsonObject.create().put("tag", index.toString()))).allRows();
            Tag b = (Tag)object;
            for(N1qlQueryRow aa:result){
                b.set.add(HashFactory.TRANSACTION.create(aa.value().getString("id")));
            }
            return b;
        }
        if(object instanceof Approvee){
            List<N1qlQueryRow> result = cluster.query(N1qlQuery.parameterized(approveesStatement, JsonObject.create().put("hash", index.toString()))).allRows();
            Approvee b = (Approvee)object;
            for(N1qlQueryRow aa:result){
                b.set.add(HashFactory.TRANSACTION.create(aa.value().getString("id")));
            }
            return b;
        }


        if(object instanceof Transaction) {
            ByteArrayDocument result = txBucket.get(index.toString(), ByteArrayDocument.class);
            if(result == null) {
                return null;
            }
            byte[] bytes = result.content();

            byte[] splitBytes = new byte[4];
            System.arraycopy(bytes, 0, splitBytes, 0, 4 );
            int split = ByteBuffer.wrap(splitBytes).getInt();
            byte[] txBytes = new byte[bytes.length - 4 - split];
            byte[] metaBytes = new byte[split];

            System.arraycopy(bytes, 4 + split, txBytes, 0, txBytes.length );
            System.arraycopy(bytes, 4, metaBytes, 0, metaBytes.length );

            //TransactionViewModel.trits();

            object.read(txBytes);
            object.readMetadata(metaBytes);

            return object;
        }

        return null;
    }





    @Override
    public Persistable seek(Class<?> model, byte[] key) throws Exception {
        Set<Indexable> hashes = keysStartingWith(model, key);
        if (hashes.isEmpty()) {
            return get(model, null);
        }
        if (hashes.size() == 1) {
            return get(model, (Indexable) hashes.toArray()[0]);
        }
        return get(model, (Indexable) hashes.toArray()[seed.nextInt(hashes.size())]);
    }



    @Override
    public boolean saveBatch(List<Pair<Indexable, Persistable>> models) throws Exception {


            for (Pair<Indexable, Persistable> entry : models) {
                save(entry.hi, entry.low);
            }

        return true;

    }

    @Override
    public boolean update(Persistable thing, Indexable index, String item) throws Exception {
        return  save(thing, index);
    }

    private boolean saveToCouchbase(Transaction tx, Hash txHash) throws Exception{
        JsonDocument newDoc = JsonDocument.create(txHash.toString(),
                JsonObject.create()
                        .put("address", tx.address.toString())
                        .put("bundle", tx.bundle.toString())
                        .put("trunk", tx.trunk.toString())
                        .put("branch", tx.branch.toString())
                        .put("tag", tx.tag.toString())
                        .put("timestamp", tx.timestamp)
        );

        byte[] txBytes = tx.bytes();
        byte[] metaBytes = tx.metadata();

        byte[] splitBytes = ByteBuffer.allocate(4).putInt(metaBytes.length).array();
        //ByteBuffer.wrap(bytes).getInt();
        //log.info("MetaData" + thing.metadata().length);
        byte[] toStore = ArrayUtils.addAll(ArrayUtils.addAll(splitBytes, metaBytes),txBytes);


        ByteArrayDocument txDoc = ByteArrayDocument.create(txHash.toString(),toStore);
        txBucket.upsert(txDoc);
        metadataBucket.upsert(newDoc);

       // Persistable result = get(Transaction.class, txHash);

        return true;
    }



    private void initDB() throws Exception {
        try {
            CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder()
                    .connectTimeout(10000) //10000ms = 10s, default is 5s
                    .build();
            this.cluster = CouchbaseCluster.create(env, this.couchbaseNodes);
            cluster.authenticate(this.couchbaseUsername, this.couchbasePassword);

            ClusterManager m = cluster.clusterManager();
            if(!m.hasBucket(txBucketName)){
                throw new Exception("Create bucket [" + txBucketName + "] on couchbase cluster");
            }
            if(!m.hasBucket(txMetaDataBucketName)){
                throw new Exception("Create bucket [" + txMetaDataBucketName + "] on couchbase cluster");
            }
            /*
                Manually apply these index creations
                CREATE PRIMARY INDEX ON `tx_bucket`;
                CREATE PRIMARY INDEX ON `tx_metadata`;
                CREATE INDEX ix_address ON `tx_metadata`(address);
                CREATE INDEX ix_bundle ON `tx_metadata`(bundle);
                CREATE INDEX ix_trunk ON `tx_metadata`(trunk);
                CREATE INDEX ix_branch ON `tx_metadata`(branch);

             */

            txBucket = cluster.openBucket(txBucketName);
            metadataBucket = cluster.openBucket(txMetaDataBucketName);
        } catch (Exception e) {
            throw e;
        }
    }

}
