package com.efficientlogfileanalysis.log;

import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.data.Tuple;
import com.efficientlogfileanalysis.data.search.Filter;
import com.efficientlogfileanalysis.data.search.SearchEntry;

import com.efficientlogfileanalysis.test.Timer;
import com.efficientlogfileanalysis.util.ByteConverter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.apache.lucene.search.*;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.GroupingSearch;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.QueryBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Has various methods to search for specific logEntries and files
 * @author Andreas Kurz
 */
public class Search implements Closeable {

    /**
     * A list with all log levels
     */
    public static final String[] allLogLevels = {"INFO", "DEBUG", "WARN", "ERROR", "TRACE", "FATAL"};

    DirectoryReader directoryReader;
    private IndexSearcher searcher;

    /**
     * Creates a new Search Object<br>
     * Opens up the Index Directory set in the settings<br>
     * @throws IOException is thrown if the directory can't be read
     */
    public Search() throws IOException
    {
        //Open the Index
        Directory indexDirectory = FSDirectory.open(Paths.get(Manager.PATH_TO_INDEX));
        directoryReader = DirectoryReader.open(indexDirectory);
        searcher = new IndexSearcher(directoryReader);
    }

    /**
     * Closes the Index
     * The Search object can't be used afterwards
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        directoryReader.close();
    }

    /**
     * Parses a given Filter into a Query Builder which can be used to get a Lucene-Query
     * @param filter specifies how the search should be filtered
     * @return a Query Builder based on the filter
     */
    private BooleanQuery.Builder parseFilter(Filter filter)
    {
        Analyzer analyzer = new StandardAnalyzer();

        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

        //Add the time range
        queryBuilder.add(
            LongPoint.newRangeQuery("date",
                    filter.getBeginDate(),
                    filter.getEndDate()
            ), BooleanClause.Occur.MUST
        );

        if(!filter.getLogLevels().isEmpty())
        {
            //Apply all logLevel filters
            //TODO improve that

            for(
                Byte notIncluded : Arrays.stream(allLogLevels)
                    //.map(LogLevelIDManager.getInstance()::get)
                    .map(Manager.getInstance()::getLogLevelID)
                    .filter(s -> !filter.getLogLevels().contains(s))
                    .collect(Collectors.toList())
            ) {
                queryBuilder.add(
                        LongPoint.newExactQuery("logLevel", notIncluded),
                        BooleanClause.Occur.MUST_NOT
                );
            }
        }

        //Apply module filter
        if(filter.getModule() != null)
        {
            //int moduleID = ModuleIDManager.getInstance().get(filter.getModule());
            int moduleID = Manager.getInstance().getModuleID(filter.getModule());
            queryBuilder.add(
                IntPoint.newExactQuery("module", moduleID),
                BooleanClause.Occur.MUST
            );
        }

        //Apply className filter
        if(filter.getClassName() != null)
        {
            //int classNameID = ClassIDManager.getInstance().get(filter.getClassName());
            int classNameID = Manager.getInstance().getClassID(filter.getClassName());
            queryBuilder.add(
                IntPoint.newExactQuery("classname", classNameID),
                BooleanClause.Occur.MUST
            );
        }

        //apply file search filter
        if(filter.getFileID() != -1)
        {
            queryBuilder.add(
                IntPoint.newExactQuery("fileIndex", filter.getFileID()),
                BooleanClause.Occur.MUST
            );
        }

        if(filter.getMessage() != null)
        {
            //Approach 1: search for sentence with sloppiness (word order can differ)
            //QueryBuilder builder = new QueryBuilder(analyzer);
            //Query messageQuery = builder.createPhraseQuery("message", filter.getMessage(), 1917);

            //Approach 2: FuzzyQuery spelling can differ
            //Term term = new Term("message", filter.getMessage());
            //FuzzyQuery messageQuery = new FuzzyQuery(term);

            try
            {
                BooleanQuery.Builder messageQueryBuilder = new BooleanQuery.Builder();

                //split the message into terms (every word)
                TokenStream tokenStream = analyzer.tokenStream("message", filter.getMessage());
                CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

                //read all terms
                tokenStream.reset();
                while (tokenStream.incrementToken())
                {
                    String token = charTermAttribute.toString();
                    Term term = new Term("message", token);

                    //add the terms as separate fuzzyQueries
                    messageQueryBuilder.add(
                        new FuzzyQuery(term),
                        BooleanClause.Occur.MUST
                    );
                }

                //add the message query
                queryBuilder.add(
                    messageQueryBuilder.build(),
                    BooleanClause.Occur.MUST
                );
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
                System.err.println("Message filter konnte nicht angewand werden!");
            }
        }

        return queryBuilder;
    }

    /**
     * Returns all Search Entries present in a search
     * @param hits an array of score docs found in a lucene search
     * @return the data for all the search entries
     * @throws IOException if the log folder can't be accessed
     */
    private List<SearchEntry> getResultsFromSearch(ScoreDoc[] hits) throws IOException
    {
        String path = Settings.getInstance().getLogFilePath();
        LogReader logReader = new LogReader();

        HashMap<Short, SearchEntry> logFiles = new HashMap<>();
        for(ScoreDoc hit : hits)
        {
            Document document = searcher.doc(hit.doc);

            short fileID = document.getField("fileIndex").numericValue().shortValue();
            long entryIndex = document.getField("logEntryID").numericValue().longValue();

            long logEntryTime = logReader.readDateOfEntry(path, fileID, entryIndex);
            String logLevel = logReader.readLogLevelOfEntry(path, fileID, entryIndex);

            //logFiles.putIfAbsent(fileIndex, new SearchEntry(FileIDManager.getInstance().get(fileID)));
            logFiles.putIfAbsent(fileID, new SearchEntry(Manager.getInstance().getFileName(fileID)));
            logFiles.get(fileID).addLogEntry(entryIndex, logLevel, logEntryTime);
        }

        logReader.close();

        return new ArrayList<>(logFiles.values());
    }

    public List<byte[]> searchForLogLevelsInFiles() throws IOException {
        ArrayList<byte[]> files = new ArrayList<>();
        Filter.FilterBuilder filterBuilder;
        Query query;
        GroupingSearch groupingSearch;
        TopGroups topGroups;
        byte[] levelsPerFile;
        int counter;

        //files.ensureCapacity(FileIDManager.getInstance().values.getKeySet().size());
        files.ensureCapacity(Manager.getInstance().getFileIDManager().values.getKeySet().size());

        //go through all files
        //for(short fileID : FileIDManager.getInstance().values.getKeySet()) {
        for(short fileID : Manager.getInstance().getFileIDManager().values.getKeySet()) {
            filterBuilder = Filter
                .builder()
                .fileID(fileID);

            //add Filter for all log levels
            for(String s : Search.allLogLevels) {
                //filterBuilder.addLogLevel(LogLevelIDManager.getInstance().get(s));
                filterBuilder.addLogLevel(Manager.getInstance().getLogLevelID(s));
            }

            query = parseFilter(filterBuilder.build()).build();
            groupingSearch = new GroupingSearch("logLevel");

            //sets how the returned groups are sorted
            //the default criteria is "RELEVANCE" which is why Field_doc (the index order) is faster
            groupingSearch.setGroupSort(new Sort(SortField.FIELD_DOC));

            //only return one result for each group:
            groupingSearch.setGroupDocsLimit(1);

            //perform group search
            topGroups = null;
            try {
                topGroups = groupingSearch.search(searcher, query, 0, Search.allLogLevels.length);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                assert topGroups != null;
            }

            counter = 0;
            levelsPerFile = new byte[topGroups.groups.length];
            for(GroupDocs gdoc : topGroups.groups)
            {
                levelsPerFile[counter++] = ((BytesRef) gdoc.groupValue).bytes[0];
            }

            files.add(fileID, levelsPerFile);
        }

        return files;
    }

    /**
     * Searches the log files with the given filter for matches and then returns information about its parent log file and all the matches as ids in that log file
     * @param filter specifies what entries should be matched
     * @return all the matching file entries
     * @throws IOException if the log files cant be accessed
     */
    public List<SearchEntry> search(Filter filter) throws IOException
    {
        Query query = parseFilter(filter).build();

        System.out.println("Lucene start...");
        ScoreDoc[] hits = searcher.search(query, Integer.MAX_VALUE).scoreDocs;
        System.out.println("Lucene finished");

        return getResultsFromSearch(hits);
    }

    private List<Long> getEntryIDsFromResult(ScoreDoc[] hits) throws IOException {
        List<Long> logEntries = new ArrayList<>();
        for(ScoreDoc hit : hits)
        {
            Document document = searcher.doc(hit.doc);
            long entryID = document.getField("logEntryID").numericValue().longValue();

            logEntries.add(entryID);
        }
        return logEntries;
    }

    /**
     * Searches for LogEntry IDs which match the given filter
     * @param filter data that every log entry needs to match
     * @return the IDs of all the matched log entries
     * @throws IOException
     */
    public List<Long> searchForLogEntryIDs(Filter filter) throws IOException
    {
        Query query = parseFilter(filter).build();

        ScoreDoc[] hits = searcher.search(query, Integer.MAX_VALUE).scoreDocs;

        return getEntryIDsFromResult(hits);
    }

    public Tuple<List<Long>, ScoreDoc> searchForLogEntryIDsWithPagination(Filter filter, int maxEntryAmount, ScoreDoc offset) throws IOException
    {
        Query query = parseFilter(filter).build();

        ScoreDoc[] hits;
        if(offset == null)
        {
            hits = searcher.search(query, maxEntryAmount).scoreDocs;
        }
        else
        {
            hits = searcher.searchAfter(offset, query, maxEntryAmount).scoreDocs;
        }

        List<Long> logEntries = getEntryIDsFromResult(hits);

        ScoreDoc lastHit = hits.length == 0 ? null : hits[hits.length-1];

        return new Tuple<>(logEntries, lastHit);
    }

    /**
     * Returns a list of files which have logEntries matched by a filter
     * @param filter other filter data that the entries need to match
     * @return a list of all matched logFileIDs
     * @throws IOException if the Index directory can't be accessed
     */
    public List<Short> searchForFiles(Filter filter) throws IOException
    {
        Query query = parseFilter(filter).build();

        GroupingSearch groupingSearch = new GroupingSearch("fileIndex");

        //sets how the returned groups are sorted
        //the default criteria is "RELEVANCE" which is why Field_doc (the index order) is faster
        groupingSearch.setGroupSort(new Sort(SortField.FIELD_DOC));

        //only return one result for each file:
        groupingSearch.setGroupDocsLimit(1);

        //perform group search
        TopGroups topGroups = groupingSearch.search(searcher, query, 0, 1000);

        List<Short> affectedFiles = new ArrayList<>();

        for(GroupDocs doc : topGroups.groups)
        {
            short fileIndex = ByteConverter.byteToShort(((BytesRef) doc.groupValue).bytes);
            affectedFiles.add(fileIndex);

            /*
                DEBUG: Print all found log entries

                ScoreDoc[] entries = doc.scoreDocs;
                LogReader logReader = new LogReader();
                for(ScoreDoc entry : entries)
                {
                    System.out.println("\t" + entry);

                    Document document = searcher.doc(entry.doc);
                    short fileIndex = document.getField("fileIndex").numericValue().shortValue();
                    long entryIndex = document.getField("logEntryID").numericValue().longValue();

                    System.out.println("\t\t" + logReader.getLogEntry(Settings.getInstance().getLogFilePath(), fileIndex, entryIndex));
                }
             */
        }

        return affectedFiles;
    }

    public static void main(String[] args) throws IOException {

        //System.out.println(Long.MAX_VALUE);

        Manager mgr = Manager.getInstance();
        Search search = new Search();
        Filter f = Filter
                .builder()
                .fileID((short) 0)
                .message("20 millisekonds runing JMSh")
                .build();

        //search.searchForFiles(f).stream().map(FileIDManager.getInstance()::get).forEach(System.out::println);

//        long fileID = FileIDManager.getInstance().get("DesktopClient-DE-GS-NB-0028.haribo.dom.log");
//        System.out.println(fileID);
//
//        Timer timer = new Timer();
//        Timer.Time time = timer.timeExecutionSpeed(() -> {
//            try {
//                List<Long> searchEntrys = search.searchForLogEntryIDs(f);
//            } catch (IOException ioe) {
//                throw new RuntimeException(ioe);
//            }
//        }, 1_000);
//
//        System.out.println(time);

        /*
        try(LogReader reader = new LogReader())
        {
            for(long id : searchEntrys)
            {
                System.out.println(id);
                System.out.println(reader.getLogEntry(Settings.getInstance().getLogFilePath(), FileIDManager.getInstance().get("DesktopClient-DE-GS-NB-0028.haribo.dom.log"), id));
            }
        }


        Timer timer = new Timer();

        Timer.Time time = timer.timeExecutionSpeed(() -> {
            try {
                search.searchForFiles(f);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 100);

        System.out.println(time);


        Timer timer = new Timer();

        Timer.Time time = timer.timeExecutionSpeed(() -> {

            try {
                Search search = new Search();

                Filter f = Filter.builder().build();
                search.search(f);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }, 1);

        System.out.println(time);

        Timer timer = new Timer();

        Timer.Time time = timer.timeExecutionSpeed(() -> {
            LogEntry result = new LogEntry("04 Jul 2022 14:27:28,743 DEBUG [key] AbstractDialog:? - hide end: MAP036 timestamp: 1656937674040");
        }, 100_000);

        System.out.println(time);
        */
    }

}
