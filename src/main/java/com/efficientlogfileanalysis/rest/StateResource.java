package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.index.IndexManager;
import com.efficientlogfileanalysis.index.data.IndexState;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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

    @POST
    @Path("/updates")
    public Response waitForStateChange(IndexState currentState)
    {
        try
        {
            if(currentState != IndexManager.getInstance().getIndexState()){
                return Response.ok(IndexManager.getInstance().getIndexState()).build();
            }

            IndexState state = IndexManager.getInstance().waitForIndexStateChange();
            return Response.ok(state).build();
        }
        catch (InterruptedException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
