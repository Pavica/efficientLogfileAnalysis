package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.data.LogEntry;
import com.efficientlogfileanalysis.data.search.Filter;
import com.efficientlogfileanalysis.data.search.SearchEntry;
import com.efficientlogfileanalysis.log.Search;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Path("/statistic")
public class StatisticResource {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private long gapTime;
    private LocalDateTime timeStampHelper;
    private LocalDateTime endOfInterval;

    private Map<String, Integer> statisticsDataMap = new HashMap<>();
    private Map<String, int[]> multiStatisticsDataMap = new HashMap<>();
    private Map<Integer, String> timeStampsMap = new HashMap<>();

    private List<Map> list = new ArrayList<>();

    private int counter = 0;

    public void setTimeSpan(long startDate, long endDate) {

        this.startDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(startDate), ZoneId.systemDefault());
        this.endDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(endDate), ZoneId.systemDefault());

        gapTime = ChronoUnit.MILLIS.between(this.startDate, this.endDate);

        gapTime = gapTime / 12;

        endOfInterval = this.startDate;

        endOfInterval = endOfInterval.plus(Duration.of(gapTime, ChronoUnit.MILLIS));

        for(int j = 0 ; j < 12 ; j++){

            timeStampHelper = LocalDateTime.ofInstant(Instant.ofEpochMilli(Duration.of(startDate, ChronoUnit.SECONDS).getSeconds()), ZoneId.systemDefault()).plus(Duration.of(gapTime, ChronoUnit.MILLIS));

            timeStampHelper = timeStampHelper.plus(Duration.of(gapTime*j, ChronoUnit.MILLIS));

            String hour = "" + timeStampHelper.getHour();
            if(hour.length() < 2)
                hour = "0" + hour;
            String minute = "" + timeStampHelper.getMinute();
            if(minute.length() < 2)
                minute = "0" + minute;
            String second = "" + timeStampHelper.getSecond();
            if(second.length() < 2)
                second = "0" + second;

            timeStampsMap.put(j+1, hour + ":" + minute + ":" + second);
        }

    }

    public void initializeMaps() {

        statisticsDataMap.clear();
        multiStatisticsDataMap.clear();
        list.clear();

        statisticsDataMap.put("INFO", 0);
        statisticsDataMap.put("DEBUG", 0);
        statisticsDataMap.put("WARN", 0);
        statisticsDataMap.put("ERROR", 0);
        statisticsDataMap.put("TRACE", 0);
        statisticsDataMap.put("FATAL", 0);

        multiStatisticsDataMap.put("INFO",  new int[]{0,0,0,0,0,0,0,0,0,0,0,0});
        multiStatisticsDataMap.put("DEBUG", new int[]{0,0,0,0,0,0,0,0,0,0,0,0});
        multiStatisticsDataMap.put("WARN",  new int[]{0,0,0,0,0,0,0,0,0,0,0,0});
        multiStatisticsDataMap.put("ERROR", new int[]{0,0,0,0,0,0,0,0,0,0,0,0});
        multiStatisticsDataMap.put("TRACE", new int[]{0,0,0,0,0,0,0,0,0,0,0,0});
        multiStatisticsDataMap.put("FATAL", new int[]{0,0,0,0,0,0,0,0,0,0,0,0});

        counter = 0;
    }

    @POST
    @Path("/sorted")
    @Produces("application/json")
    public Response sortedStatistics(SearchResource.FilterData filterData) throws IOException {

        initializeMaps();
        setTimeSpan(filterData.getBeginDate(), filterData.getEndDate());

        Filter filter = SearchResource.parseFilterData(filterData);

        try {
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
        }
    }

    public void addLogLevelData(String logLevel) {
        int value = statisticsDataMap.get(logLevel);
        value++;
        statisticsDataMap.put(logLevel, value);
    }

    public void addLogLevelDataDependingOnTime(LogEntry data) {
        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(data.getTime()), ZoneId.systemDefault());
        if (counter <= 11) {
            if (date.isBefore(endOfInterval)) {
                int[] value = multiStatisticsDataMap.get(data.getLogLevel());

                value[counter]++;
                multiStatisticsDataMap.put(data.getLogLevel(), value);

            } else {
                endOfInterval = endOfInterval.plus(Duration.of(gapTime, ChronoUnit.MILLIS));

                counter++;
                if (counter <= 11) {
                    int[] value = multiStatisticsDataMap.get(data.getLogLevel());
                    value[counter]++;
                    multiStatisticsDataMap.put(data.getLogLevel(), value);

                }
            }
        }
    }
}
