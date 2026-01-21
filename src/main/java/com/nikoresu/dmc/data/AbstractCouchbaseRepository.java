package com.nikoresu.dmc.data;

import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.query.QueryResult;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class AbstractCouchbaseRepository<T, ID> implements CouchbaseRepository<T, ID> {
    @Inject
    protected Cluster cluster;

    protected Collection collection;
    private final Class<T> entityClass;

    protected AbstractCouchbaseRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @PostConstruct
    void initialize() {
        var bucket = cluster.bucket(getBucketName());
        bucket.waitUntilReady(Duration.ofSeconds(30));
        this.collection = bucket.scope(getScopeName()).collection(getCollectionName());
        log.info("Initialized repository for {}", entityClass.getSimpleName());
    }

    protected abstract String getBucketName();

    protected abstract String getScopeName();

    protected abstract String getCollectionName();

    @Override
    public Optional<T> findById(ID id) {
        try {
            T result = collection.get(String.valueOf(id)).contentAs(entityClass);
            return Optional.of(result);
        } catch (DocumentNotFoundException e) {
            log.debug("Document not found with id: {}", id);
            return Optional.empty();
        }
    }

    @Override
    public void save(ID id, T entity) {
        collection.upsert(String.valueOf(id), entity);
        log.debug("Saved document with id: {}", id);
    }

    @Override
    public void deleteById(ID id) {
        try {
            collection.remove(String.valueOf(id));
            log.debug("Deleted document with id: {}", id);
        } catch (DocumentNotFoundException e) {
            log.debug("Document not found for deletion with id: {}", id);
        }
    }

    @Override
    public boolean exists(ID id) {
        return collection.exists(String.valueOf(id)).exists();
    }

    @Override
    public List<T> findAll(int limit) {
        String query = String.format(
                "SELECT META().id, d.* FROM `%s`.`%s`.`%s` d LIMIT %d",
                getBucketName(), getScopeName(), getCollectionName(), limit
        );

        QueryResult result = cluster.query(query);
        return result.rowsAs(entityClass);
    }

    protected List<T> executeQuery(String query) {
        QueryResult result = cluster.query(query);
        return result.rowsAs(entityClass);
    }
}
