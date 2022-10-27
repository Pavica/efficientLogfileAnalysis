package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.index.IndexManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/state")
public class StateResource {

    /**
     * Retrieves the current state of the Index
     * @return the current Index state as a string
     */
    @GET
    public Response getState()
    {
        return Response.ok(IndexManager.getInstance().getIndexState()).build();
    }
}
