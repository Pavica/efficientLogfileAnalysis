package com.efficientlogfileanalysis.rest;

import com.efficientlogfileanalysis.index.Index;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@WebListener
@ApplicationPath("/api")
public class LogFileApplication extends Application implements ServletContextListener {

    public void contextInitialized(ServletContextEvent e) {
        Index.getInstance().startIndexCreationWorker();
    }

    public void contextDestroyed(ServletContextEvent e) {
        Index.getInstance().stopIndexCreationWorker();
    }
}