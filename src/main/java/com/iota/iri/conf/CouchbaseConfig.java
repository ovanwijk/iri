package com.iota.iri.conf;

import java.util.ArrayList;
import java.util.List;

public interface CouchbaseConfig extends Config {

    boolean isCouchBaseEnabled();

    String[] getCouchbaseNodes();

    String getCouchbaseUsername();

    String getCouchbasePassword();

    String getTxBucketName();

    String getMetdataBucketName();

    interface Descriptions {
        String COUCHBASE_ENABLED = "Enabling couchbase";
        String COUCHBASE_NODES = "List of nodes by ip or hostname";
        String COUCHBASE_USERNAME = "Couchbase username";
        String COUCHBASE_PASSWORD = "Couchbase password";
        String COUCHBASE_TXBUCKET = "Bucket name to store binary TX data in";
        String COUCHBASE_METADATABUCKET = "Bucket name to store metadata and indexes in";

    }
}
