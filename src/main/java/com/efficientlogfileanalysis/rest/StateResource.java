package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.log.IndexManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/state")
public class StateResource {

    @GET
    public Response getState()
    {
        return Response.ok(IndexManager.getInstance().getIndexState()).build();
    }
}
