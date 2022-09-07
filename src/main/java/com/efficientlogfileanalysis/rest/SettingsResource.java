package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.log.IndexManager;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.io.File;
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
        File logFolder = new File(newPath);

        //prevent invalid log directory paths
        if( !(logFolder.exists() && logFolder.isDirectory()) )
        {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try
        {
            Settings settings = Settings.getInstance();
            settings.setLogFilePath(newPath);

            IndexManager.getInstance().startIndexCreationWorker();
        }
        catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok().build();
    }

}
