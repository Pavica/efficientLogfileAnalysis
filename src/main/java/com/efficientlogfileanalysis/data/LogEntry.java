package com.efficientlogfileanalysis.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Data class representing a single log entry.
 * @author Andreas Kurz, Jan Mandl
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogEntry
{
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd LLL yyyy HH:mm:ss,SSS").withLocale(Locale.ENGLISH);

    private long time;
    private String logLevel;
    private String module;
    private String className;
    private String message;
    
    public LogEntry(String logEntry)
    {
        setLocalDateTime(logEntry.substring(0, 24));
        this.logLevel = logEntry.substring(24, 31).trim();

        int indexOfClosedParenthesis = logEntry.indexOf("]", 32);
        this.module = logEntry.substring(32, indexOfClosedParenthesis);

        int indexOfColon = logEntry.indexOf(":", indexOfClosedParenthesis + 2);
        this.className = logEntry.substring(indexOfClosedParenthesis + 2, indexOfColon);
        this.message = logEntry.substring(logEntry.indexOf(" - ", indexOfColon) + 3);
    }

    public LogEntry(String time, String logLevel, String module, String className, String message)
    {
        setLocalDateTime(time);
        this.logLevel = logLevel;
        this.module = module;
        this.className = className;
        this.message = message;
    }

    public void setLocalDateTime(String time)
    {
        this.time = LocalDateTime.parse(time, DTF).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public LocalDateTime getLocalDateTime()
    {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    }

    @Override
    public String toString()
    {
        return String.format("%s %s [%s] %s:? - %s", getLocalDateTime().format(DTF), logLevel, module, className, message);
    }

    public static void main(String[] args)
    {
        //LogEntry logEntry = new LogEntry("14 Apr 2022 13:42:32,798", "INFO", "main", "LocalServiceProvider", "destroying service platform.jdbc.xa.XaManagerService");
        //LogEntry logEntry = new LogEntry("14 Apr 2022 13:42:32,798  INFO [main] LocalServiceProvider:? - destroying service platform.jdbc.xa.XaManagerService");
        LogEntry logEntry = new LogEntry("05 Jul 2022 12:53:59,257  INFO [NGKP-RCV Chl: 1, Port: 2012] DefaultSocTelegramHandler:? - Send telegram: TT0811(1->201)##RequestId=21396557|ToNr=434|ToTrId=0#|CurrentPos=(28/1)|LocalDestinations=(33/2)(74/1)(0/0)(0/0)(0/0)|OrderCode=8003|NoCheckOfReqId|AckCode=8003| // TT0811Subtype1Version0:senderId=1|receiverId=201|type=811|subType=1|version=0|reserve=0|requestId=21396557|toNr=434|toTrId=0|CuNr=28|PosNr=1|CuNr=33|PosNr=2|CuNr=74|PosNr=1|CuNr=0|PosNr=0|CuNr=0|PosNr=0|CuNr=0|PosNr=0|orderCode=8003|orderExtension1={1}|orderExtension2={}|acknowledge=8003|ackExtension1={}|ackExtension2={}");
        System.out.println(logEntry);
    }
}
