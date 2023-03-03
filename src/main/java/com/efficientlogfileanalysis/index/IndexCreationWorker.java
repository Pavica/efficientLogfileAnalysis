package com.efficientlogfileanalysis.index;

import com.efficientlogfileanalysis.data.ConcurrentQueue;
import com.efficientlogfileanalysis.data.Settings;
import com.efficientlogfileanalysis.index.data.IndexCreatorTask;
import com.efficientlogfileanalysis.index.data.IndexState;
import com.efficientlogfileanalysis.logs.LogReader;
import com.efficientlogfileanalysis.util.Timer;

import java.io.File;
import java.io.IOException;

/**
 * Thread which performs Indexing Operations
 */
class IndexCreationWorker extends Thread{

    private Index index;

    private ConcurrentQueue<IndexCreatorTask> tasks = new ConcurrentQueue<>();
    private DirectoryWatcher fileChangeChecker;

    private boolean directoryChanged = false;
    private boolean interrupted = false;

    public IndexCreationWorker(Index index)
    {
        this.index = index;
        this.fileChangeChecker = new DirectoryWatcher(Settings.getInstance().getLogFilePath(), tasks::push);
    }

    public void run()
    {
        fileChangeChecker = new DirectoryWatcher(Settings.getInstance().getLogFilePath(), tasks::push);
        fileChangeChecker.start();

        try
        {
            index.setCurrentState(IndexState.INDEXING);
            index.readIndices();
            checkAllFilesForUpdates();
            index.setCurrentState(IndexState.READY);
        }
        catch (InterruptedException e) {interrupted = true;}
        catch (IOException e) {e.printStackTrace();}

        while(!interrupted || directoryChanged)
        {
            try
            {
                while(directoryChanged){
                    interrupted = false;
                    System.out.println("Recreating Index");
                    directoryChanged = false;
                    index.setCurrentState(IndexState.INDEXING);
                    Timer timer = new Timer();
                    createNewIndex();
                    System.out.println("Index Creation took: " + timer.time());
                    index.setCurrentState(IndexState.READY);
                }

                IndexCreatorTask task = tasks.pop();

                index.setCurrentState(IndexState.INDEXING);
                switch(task.getTaskType())
                {
                    case FILE_CREATED:
                        fileCreated(task.getFilename());
                        break;
                    case FILE_APPENDED:
                        fileChanged(task.getFilename());
                        break;
                }

                if(tasks.isEmpty()){
                    index.setCurrentState(IndexState.READY);
                }
            }
            catch (InterruptedException e) {
                index.setCurrentState(IndexState.INTERRUPTED);
                interrupted = true;
            }
            catch (IOException e){
                //--- An error occurred --//
                System.err.println("Everything died");
                e.printStackTrace();
                index.setCurrentState(IndexState.ERROR);
                return;
            }
        }
    }

    private void fileCreated(String filename) throws IOException, InterruptedException {
//            System.out.println("Indexing new file: " + filename);
        updateFile(filename);
    }

    private void fileChanged(String filename) throws IOException, InterruptedException {
//            System.out.println("Indexing changes in file: " + filename);
        updateFile(filename);
    }

    public void redoIndex()
    {
        directoryChanged = true;
        this.interrupt();
    }

    public void shutdown()
    {
        this.interrupt();
        fileChangeChecker.interrupt();
    }

    private void createNewIndex()
    {
        try
        {
            index.deleteIndex();

            try(IndexCreator indexCreator = new IndexCreator(index))
            {
                fileChangeChecker = fileChangeChecker.switchDirectory(
                        Settings.getInstance().getLogFilePath()
                );

                String logFolder = Settings.getInstance().getLogFilePath();

                LogReader.forEachLogFile(logFolder,
                    filePath -> indexCreator.indexSingleLogFile(filePath.toFile().getName())
                );

                index.saveIndices();
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private void checkAllFilesForUpdates() throws IOException, InterruptedException
    {
        LogReader.forEachLogFile(Settings.getInstance().getLogFilePath(),
            filePath -> updateFile(filePath.getFileName().toString())
        );
    }

    private void updateFile(String filename) throws IOException, InterruptedException
    {
        File file = new File(Settings.getInstance().getLogFilePath() + File.separator + filename);

        //ignore all files that don't end in .log
        if(!file.exists() || file.isDirectory() || !file.getName().endsWith(".log")){
            return;
        }

        try(IndexCreator indexCreator = new IndexCreator(index))
        {
            indexCreator.repeatablyTryAndUpdateFile(file);
        }

        index.saveIndices();
    }
}
