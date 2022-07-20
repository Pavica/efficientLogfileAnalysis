package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.data.LogEntry;
import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.log.FileIDManager;
import com.efficientlogfileanalysis.log.LogReader;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Path("/logFiles")
public class LogFileResource {

    @GET
    @Produces("application/json")
    public Response getAllLogfiles()
    {
        return Response.ok(FileIDManager.getInstance().getLogFileData()).build();
    }

    @GET
    @Path("/{logFileName}/{id}")
    @Produces("application/json")
    public Response getLogEntry(
        @PathParam("logFileName") String logFileName,
        @PathParam("id") long entryID
    )
    {
        short fileID = FileIDManager.getInstance().get(logFileName);

        try( LogReader logReader = new LogReader())
        {
            LogEntry requestedEntry = logReader.getLogEntry(
                Settings.getInstance().getLogFilePath(),
                fileID,
                entryID
            );

            return Response.ok(requestedEntry).build();
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/{logFileName}/specificEntries")
    @Produces("application/json")
    public Response getLogEntries(
        @PathParam("logFileName") String logFileName,
        List<Long> requestedIDs
    )
    {
        short fileID = FileIDManager.getInstance().get(logFileName);

        try( LogReader logReader = new LogReader())
        {
            String logFilePath = Settings.getInstance().getLogFilePath();

            List<LogEntry> logEntries = new ArrayList<>();

            for(long entryID : requestedIDs)
            {
                logEntries.add(logReader.getLogEntry(
                    logFilePath,
                    fileID,
                    entryID
                ));
            }

            return Response.ok(logEntries).build();
        }
        catch(IOException exception)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


}
