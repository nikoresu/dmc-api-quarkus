package com.nikoresu.dmc.config;

import com.couchbase.client.java.Cluster;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Getter
@ApplicationScoped
public class CouchbaseClientConfig {
    // Add '=manager' at the end of connectionUrl to change mgmt port
    private final String connectionUrl;
    private final String username;
    private final String password;

    private Cluster cluster;

    public CouchbaseClientConfig(
            @ConfigProperty(name = "couchbase.connection.url") String connectionUrl,
            @ConfigProperty(name = "couchbase.connection.username") String username,
            @ConfigProperty(name = "couchbase.connection.password") String password
    ) {
        this.connectionUrl = connectionUrl;
        this.username = username;
        this.password = password;
    }

    @PostConstruct
    private void initializeCluster() {
        this.cluster =  Cluster.connect(connectionUrl, username, password);
    }
}
