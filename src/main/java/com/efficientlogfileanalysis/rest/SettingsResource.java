package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.data.Settings;
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
            return Response.ok().build();
        }
        catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
