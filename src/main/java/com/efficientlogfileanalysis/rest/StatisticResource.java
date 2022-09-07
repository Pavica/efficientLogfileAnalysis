package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.data.LogEntry;
import com.efficientlogfileanalysis.data.search.Filter;
import com.efficientlogfileanalysis.log.Search;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Data class representing a single log entry.
 * @author Clark Jaindl
 * last changed: 01.08.2022
 */
@Path("/statistic")
public class StatisticResource {

    /** contains the start date which is the beginning of the period being searched in */
    private LocalDateTime startDate;

    /** contains the end date which is the end of the period being searched in */
    private LocalDateTime endDate;

    /** contains the time between the sections on the multilineChart */
    private long gapTime;

    /** helper variable for the timestamps on the multilineChart */
    private LocalDateTime timeStampHelper;

    /** contains the end of timestamp intervals on the multilineChart */
    private LocalDateTime endOfInterval;

    private List<LocalDateTime> endOfIntervalsList = new ArrayList<>();

    /** list of all existing loglevel types */
    private List<String> logLevelList = new ArrayList<String>(){{
        add("INFO");
        add("DEBUG");
        add("WARN");
        add("ERROR");
        add("TRACE");
        add("FATAL");
    }};

    private List<HashMap<String, Integer>> statisticsData = new ArrayList<>();

    /** map containing timestamps for the multilineChart */
    private List<String> timeStampsList = new ArrayList<>();

    /** list containing statisticsDataMap, multiStatisticsDataMap and timeStampsMap */
    private List<List> list = new ArrayList<>();

    /** counter variable used to identify each section on the multilineChart*/
    private int counter = 0;


    /**
     * Function used to set the end of each interval, the time between each section
     *          and format the time being displayed on the multilineChart
     *
     * @param startDate beginning of the period being searched in
     * @param endDate end of the period being searched in
     */
    public void setTimeSpan(long startDate, long endDate) {

        this.startDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(startDate), ZoneId.systemDefault());
        this.endDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(endDate), ZoneId.systemDefault());

        gapTime = ChronoUnit.MILLIS.between(this.startDate, this.endDate);

        gapTime = gapTime / 12;

        endOfInterval = this.startDate;

        endOfInterval = endOfInterval.plus(Duration.of(gapTime, ChronoUnit.MILLIS));

        for (int j = 0; j < 12; j++) {

            timeStampHelper = LocalDateTime.ofInstant(Instant.ofEpochMilli(Duration.of(startDate, ChronoUnit.SECONDS).getSeconds()), ZoneId.systemDefault()).plus(Duration.of(gapTime, ChronoUnit.MILLIS));

            timeStampHelper = timeStampHelper.plus(Duration.of(gapTime * j, ChronoUnit.MILLIS));

            endOfIntervalsList.add(timeStampHelper);

            String hour = String.format("%02d", timeStampHelper.getHour());
            String minute = String.format("%02d", timeStampHelper.getMinute());
            String second = String.format("%02d", timeStampHelper.getSecond());
            String day = String.format("%02d", timeStampHelper.getDayOfMonth());
            String month = String.format("%02d", timeStampHelper.getMonthValue());

            if (this.startDate.getDayOfYear() != this.endDate.getDayOfYear()) {
                timeStampsList.add(day + "." + month + " " + hour + ":" + minute + ":" + second);
            } else {
                timeStampsList.add(hour + ":" + minute + ":" + second);
            }
        }
    }

//    /**
//     * Function used to initialize all maps after every new search.
//     */
//    public void initializeMaps() {
//
//        statisticsDataMap.clear();
//        multiStatisticsDataMap.clear();
//        list.clear();
//
//        logLevelList.forEach(e -> {
//            statisticsDataMap.put(e, 0);
//            multiStatisticsDataMap.put(e, new int[]{0,0,0,0,0,0,0,0,0,0,0,0});
//        });
//
//        counter = 0;
//    }

    /**
     * Function used to fill the maps with data for the statistics
     *
     * @param filterData contains all information from the filter-section
     * @return Response of the list which contains all three maps
     * @throws IOException
     */
    @POST
    @Path("/data")
    @Produces("application/json")
    public Response sortedStatistics(SearchResource.FilterData filterData) throws IOException {

        Search search = new Search();

        Filter filter = SearchResource.parseFilterData(filterData);

        setTimeSpan(filter.getBeginDate(), filter.getEndDate());

        for(int i = 0 ; i < 12 ; i++){
            filter.setEndDate(LogEntry.toLong(endOfIntervalsList.get(i)));
            filter.setBeginDate(LogEntry.toLong(endOfIntervalsList.get(i).minus(Duration.of(gapTime, ChronoUnit.MILLIS))));
            HashMap<String, Integer> map = search.getLogLevelCount(filter);

            statisticsData.add(map);
        }

        list.add(statisticsData);
        list.add(timeStampsList);

        return Response.ok(list).build();

        /*

        initializeMaps();

        try (Search search = new Search())
        {
            List<LogEntry> helper;

            Search search = new Search();

            helper = search.sortedSearch(filter);

            for (int i = 0; i < helper.size(); i++) {

                LogEntry data = helper.get(i);
                addLogLevelData(data.getLogLevel());
                addLogLevelDataDependingOnTime(data);
            }

            list.add(statisticsDataMap);
            list.add(multiStatisticsDataMap);
            list.add(timeStampsMap);

            return Response.ok(list).build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }*/
    }

//    /**
//     * Function used to determine the quantity of each loglevel.
//     * Used for all statistics except multilineChart
//     *
//     * @param logLevel logLevel which amount has to be raised
//     */
//    public void addLogLevelData(String logLevel) {
//        int value = statisticsDataMap.get(logLevel);
//        value++;
//        statisticsDataMap.put(logLevel, value);
//    }
//
//    /**
//     * Function used to determine the quantity of each loglevel for each section on the multilineChart
//     *
//     * @param data logEntry which is one line in a logfile
//     */
//    public void addLogLevelDataDependingOnTime(LogEntry data) {
//        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(data.getTime()), ZoneId.systemDefault());
//        if (counter <= 11) {
//            if (date.isBefore(endOfInterval)) {
//                int[] value = multiStatisticsDataMap.get(data.getLogLevel());
//
//                value[counter]++;
//                multiStatisticsDataMap.put(data.getLogLevel(), value);
//
//            } else {
//                endOfInterval = endOfInterval.plus(Duration.of(gapTime, ChronoUnit.MILLIS));
//
//                counter++;
//                if (counter <= 11) {
//                    int[] value = multiStatisticsDataMap.get(data.getLogLevel());
//                    value[counter]++;
//                    multiStatisticsDataMap.put(data.getLogLevel(), value);
//
//                }
//            }
//        }
//    }
}
