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
    public Response waitForStateChange(String currentState)
    {
        try
        {
            //check if the state of the client is already different
            IndexState currentClientState = IndexState.valueOf(currentState);

            if(currentClientState != IndexManager.getInstance().getIndexState()){
                return Response.ok(IndexManager.getInstance().getIndexState()).build();
            }
        }
        //is thrown if the state of the client isn't valid. If so, immediately return the correct index state
        catch(IllegalArgumentException illegalArgument){
            return Response.ok(IndexManager.getInstance().getIndexState()).build();
        }

        //The client state is up-to-date, which means the server waits for an Index Change
        try
        {
            //wait for state change
            IndexState state = IndexManager.getInstance().waitForIndexStateChange(30);

            if(state == null){
                //timeout was reached
                return Response.status(Response.Status.NO_CONTENT).build();
            }

            return Response.ok(state).build();
        }
        catch (InterruptedException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
