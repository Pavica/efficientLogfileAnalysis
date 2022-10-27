package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.index.IndexManager;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;

@Path("/settings")
public class SettingsResource {

    /**
     * Returns the path to the LogFile folder
     * @return
     *      200 - OK -> The path was successfully retrieved
     *      500 - INTERNAL_SERVER_ERROR -> The path could not be read
     */
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

    /**
     * Sets the path to the LogFile folder and initiates Indexation
     * @param newPath the new path of the LogFile folder
     * @return
     *      200 - OK -> If the path has successfully been set
     *      400 - BAD_REQUEST -> If the Folder doesn't exist or the path is invalid
     *      500 - INTERNAL_SERVER_ERROR -> The Path couldn't be set or the Index worker couldn't be started
     */
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
