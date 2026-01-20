package com.nikoresu.dmc.data;

import com.couchbase.client.core.error.ScopeNotFoundException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.manager.collection.CollectionManager;
import com.nikoresu.dmc.config.CouchbaseClientConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

@ApplicationScoped
@Slf4j
public class DmcBucketRepository {
    @Getter
    private Collection dmcEntriesCollection;

    private Bucket dmcBucket;

    private final CouchbaseClientConfig couchbaseClient;
    private final String bucketName;
    private final String bucketScope;
    private final String bucketCollection;

    public DmcBucketRepository(
            @ConfigProperty(name = "couchbase.dmc.bucketName") String bucketName,
            @ConfigProperty(name = "couchbase.dmc.bucketScope") String bucketScope,
            @ConfigProperty(name =  "couchbase.dmc.bucketCollection") String bucketCollection,
            CouchbaseClientConfig couchbaseClient
    ) {
        this.couchbaseClient = couchbaseClient;
        this.bucketName = bucketName;
        this.bucketScope = bucketScope;
        this.bucketCollection = bucketCollection;
    }

    @PostConstruct
    private void initializeBucket() {
        this.dmcBucket = couchbaseClient.getCluster().bucket(bucketName);
        this.dmcBucket.waitUntilReady(Duration.ofSeconds(30));

        this.dmcEntriesCollection = this.dmcBucket.scope(bucketScope).collection(bucketCollection);
    }

    private void ensureScopeExists() {
        // Skip if using the default scope
        if ("_default".equals(bucketScope)) {
            log.info("Using default scope, no need to create");
            return;
        }

        CollectionManager collectionMgr = dmcBucket.collections();

        try {
            // Check if scope exists by trying to get it
            collectionMgr.getScope(bucketScope);
            log.info("Scope '{}' already exists", bucketScope);
        } catch (ScopeNotFoundException e) {
            log.info("Scope '{}' not found, creating it...", bucketScope);
            collectionMgr.createScope(bucketScope);
            log.info("Scope '{}' created successfully", bucketScope);
        }
    }

}
