package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.log.IndexManager;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.io.IOException;

@Path("/settings")
public class SettingsResource {

    @GET
    @Path("/path")
    public Response getPath()
    {
        try
        {
            Settings settings = Settings.getInstance();
            return Response.ok(settings.getLogFilePath()).build();
        }
        catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/path")
    public Response setPath(String newPath)
    {
        try
        {
            Settings settings = Settings.getInstance();
            settings.setLogFilePath(newPath);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(!IndexManager.getInstance().createIndices()) {
                            System.out.println("Index not created");
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            });
            return Response.ok().build();
        }
        catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
