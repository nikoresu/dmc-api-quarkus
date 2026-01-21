package com.nikoresu.dmc.data;

import com.nikoresu.dmc.domain.DmcEntry;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.List;

@ApplicationScoped
@Slf4j
public class DmcEntryRepository extends AbstractCouchbaseRepository<DmcEntry, String> {
    @ConfigProperty(name = "couchbase.dmc.bucketScope")
    String scopeName;

    @ConfigProperty(name = "couchbase.dmc.bucketName")
    String bucketName;

    @ConfigProperty(name = "couchbase.dmc.bucketCollection")
    String collectionName;

    public DmcEntryRepository() {
        super(DmcEntry.class);
    }

    @Override
    protected String getBucketName() {
        return bucketName;
    }

    @Override
    protected String getScopeName() {
        return scopeName;
    }

    @Override
    protected String getCollectionName() {
        return collectionName;
    }

    public List<DmcEntry> findByCodeContaining(String code) {
        String query = String.format(
                "SELECT META().id, d.* FROM `%s`.`%s`.`%s` d WHERE d.code LIKE '%%%s%%'",
                bucketName, scopeName, collectionName, code
        );
        return executeQuery(query);
    }

    public List<DmcEntry> findByThreadColor(String color) {
        String query = String.format(
                "SELECT META().id, d.* FROM `%s`.`%s`.`%s` d WHERE d.thread_color = '%s'",
                bucketName, scopeName, collectionName, color
        );
        return executeQuery(query);
    }

}
