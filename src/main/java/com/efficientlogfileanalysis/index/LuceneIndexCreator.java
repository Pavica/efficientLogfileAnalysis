package com.efficientlogfileanalysis.index;

import lombok.Getter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

@Getter
public class LuceneIndexCreator implements Closeable {

    private static final Path LUCENE_DIRECTORY = IndexManager.PATH_TO_INDEX.resolve("lucene");

    private Directory indexDirectory;
    private Analyzer analyzer;
    private IndexWriterConfig indexWriterConfig;
    private IndexWriter indexWriter;

    public LuceneIndexCreator() throws IOException
    {
        //Open the index directory (creates the directory if it doesn't exist)
        indexDirectory = FSDirectory.open(LUCENE_DIRECTORY);

        //Create Analyzer object
        //The analyzer removes useless tokens ( words like a, an is etc.)
        analyzer = new StandardAnalyzer();

        //The IndexWriter is used to create an Index
        indexWriterConfig = new IndexWriterConfig(analyzer);

        //Specify the Index to be written to and the config
        indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);
    }

    @Override
    public void close() throws IOException
    {
        indexWriter.close();
        analyzer.close();
        indexDirectory.close();
    }
}
