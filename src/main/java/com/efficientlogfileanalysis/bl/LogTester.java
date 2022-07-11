package com.efficientlogfileanalysis.bl;

import java.io.*;

public class LogTester {

    public static void main(String[] args) throws IOException {

        FileIDManager idManager = FileIDManager.getInstance();
        System.out.println(idManager.getFileID("asdf"));

//        List<LogEntry> logEntries = new ArrayList<>();
//        File logFolder = new File("test_logs");
//        for(File file : logFolder.listFiles())
//        {
//            BufferedReader br = new BufferedReader(new FileReader(file));
//
//            String line = "";
//            String currentLine;
//
//            int lineNumber = 0;
//            while((currentLine = br.readLine()) != null)
//            {
//                lineNumber++;
//
//                if(line.equals("") || !currentLine.matches("\\d{2} \\w{3} \\d{4}.*"))
//                {
//                    line += currentLine + "\n";
//                    continue;
//                }
//
//                LogEntry newLogEntry = new LogEntry(line);
//                line = currentLine;
//
//                //temporary
//                logEntries.add(newLogEntry);
//            }
//        }
    }
}
