package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.logs.data.LogEntry;
import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.index.Index;
import com.efficientlogfileanalysis.logs.LogReader;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/logFiles")
public class LogFileResource {

    @GET
    @Produces("application/json")
    public Response getAllLogfiles()
    {
        //return Response.ok(FileIDManager.getInstance().getLogFileData()).build();
        return Response.ok(Index.getInstance().getFileData()).build();
    }

    @GET
    @Path("/{logFileName}/{id}")
    @Produces("application/json")
    public Response getLogEntry(
        @PathParam("logFileName") String logFileName,
        @PathParam("id") long entryID
    )
    {
        try( LogReader logReader = new LogReader(Settings.getInstance().getLogFilePath()))
        {
            LogEntry requestedEntry = logReader.getLogEntry(
                logFileName,
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
        try( LogReader logReader = new LogReader(Settings.getInstance().getLogFilePath()))
        {
            List<LogEntry> logEntries = new ArrayList<>();

            for(long entryID : requestedIDs)
            {
                logEntries.add(logReader.getLogEntry(
                    logFileName,
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

    @GET
    @Path("/nearby")
    @Produces("application/json")
    public Response getNearbyEntries(
            @QueryParam("filename") String filename,
            @QueryParam("entryID") long entryID,
            @QueryParam("byteRange") int byteRange
    )
    {
        try(LogReader reader = new LogReader(Settings.getInstance().getLogFilePath()))
        {
            return Response.ok(reader.getNearbyEntries(filename, entryID, byteRange)).build();
        }
        catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Data
    @AllArgsConstructor
    public static class RawEntryData{
        private long entryID;
        private String entry;
    }

    @GET
    @Path("/nearbyRaw")
    @Produces("application/json")
    public Response getNearbyEntriesRaw(
            @QueryParam("filename") String filename,
            @QueryParam("entryID") long entryID,
            @QueryParam("byteRange") int byteRange
    )
    {
        try(LogReader reader = new LogReader(Settings.getInstance().getLogFilePath()))
        {
            short fileID = Index.getInstance().getFileID(filename);
            List<LogEntry> entries = reader.getNearbyEntries(filename, entryID, byteRange);

            List<RawEntryData> rawData = entries.stream().map(e -> new RawEntryData(e.getEntryID(), e.toString())).collect(Collectors.toList());
            return Response.ok(rawData).build();
        }
        catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("classNames")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClassNames() {
        Set<String> classNames = Index.getInstance().getClassNames();
        return Response.ok(classNames).type(MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("modules")
    public Response getAllModules()
    {
        Set<String> moduleNames = Index.getInstance().getModuleNames();
        return Response.ok(moduleNames).build();
    }

    @GET
    @Path("exceptions")
    public Response getAllExceptions()
    {
        Set<String> moduleNames = Index.getInstance().getExceptionNames();
        return Response.ok(moduleNames).build();
    }
}
