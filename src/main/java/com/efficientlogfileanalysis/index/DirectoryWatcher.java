package com.efficientlogfileanalysis.index;

import com.efficientlogfileanalysis.index.data.IndexCreatorTask;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class DirectoryWatcher extends Thread
{
    public interface FileEventListener {
        void handleTasks(List<IndexCreatorTask> task);
    }

    public DirectoryWatcher switchDirectory(String newPath)
    {
        DirectoryWatcher newWatcher = new DirectoryWatcher(newPath, eventListener);
        this.interrupt();
        newWatcher.start();
        return newWatcher;
    }

    private String directoryPath;
    private FileEventListener eventListener;

    @Override
    public void run()
    {
        WatchService watchService;

        try
        {
            watchService = FileSystems.getDefault().newWatchService();
            Paths.get(directoryPath).register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
            );
        }
        catch (IOException ioException)
        {
            System.err.println("Files could not be watched!");
            return;
        }

        try
        {
            while (!Thread.currentThread().isInterrupted())
            {
                WatchKey key = watchService.take();

                List<IndexCreatorTask> tasks = new ArrayList<>();

                for (WatchEvent event : key.pollEvents())
                {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        System.err.println("Overflow");
                        continue;
                    }

                    Path fileName = (Path) event.context();
                    File file = Paths.get(directoryPath).resolve(fileName).toFile();

                    tasks.add(new IndexCreatorTask(
                        file.getName(),
                        event.kind() == StandardWatchEventKinds.ENTRY_CREATE ? IndexCreatorTask.TaskType.FILE_CREATED :
                        event.kind() == StandardWatchEventKinds.ENTRY_MODIFY ? IndexCreatorTask.TaskType.FILE_APPENDED :
                        IndexCreatorTask.TaskType.FILE_DELETED
                    ));
                }

                eventListener.handleTasks(tasks);
                key.reset();
            }
        }
        catch (Exception interruptedException) {
            System.err.println("Directory Watcher was interrupted");
            interruptedException.printStackTrace();
        }
    }
}
