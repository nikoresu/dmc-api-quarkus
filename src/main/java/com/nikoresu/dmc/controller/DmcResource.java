package com.nikoresu.dmc.controller;

import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.nikoresu.dmc.data.DmcBucketRepository;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/dmc")
public class DmcResource {
    private DmcBucketRepository dmcRepository;

    public DmcResource(DmcBucketRepository dmcRepository) {
        this.dmcRepository = dmcRepository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Object getDmc() {
        GetResult result = dmcRepository.getDmcEntriesCollection().get(String.valueOf(1));
        JsonObject content = result.contentAsObject();
        return content.get("hello");
    }
}
