package com.efficientlogfileanalysis.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/state")
public class StateResource {

    @GET
    public Response getState()
    {
        return Response.ok("Ready").build();
    }
}
