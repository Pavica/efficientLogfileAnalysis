package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.log.IndexManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.io.IOException;

@WebListener
@ApplicationPath("/api")
public class LogFileApplication extends Application implements ServletContextListener {

    public void contextInitialized(ServletContextEvent e) {
        try
        {
            IndexManager.getInstance().readIndices();
        }
        catch (IOException ex)
        {
            //if the index is not present start to index the files
            IndexManager.getInstance().startIndexCreationWorker();
        }
    }

    public void contextDestroyed(ServletContextEvent e) {
        System.out.println("Server stopped");
    }
}