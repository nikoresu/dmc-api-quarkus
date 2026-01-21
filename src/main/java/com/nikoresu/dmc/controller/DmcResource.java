package com.nikoresu.controller;

import com.nikoresu.dmc.service.DmcEntryService;
import com.nikoresu.dmc.domain.DmcEntry;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Path("/dmc")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DmcResource {
    @Inject
    DmcEntryService dmcService;

    public record ErrorResponse(String code, String message) {
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") String id) {
        return dmcService.getById(id)
                .map(entry -> Response.ok(entry).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("404", "Entry not found with id: " + id))
                        .build());
    }

    @GET
    public Response getAll(@QueryParam("limit") @DefaultValue("100") int limit) {
        List<DmcEntry> entries = dmcService.getAllEntries(limit);
        return Response.ok(entries).build();
    }
}
