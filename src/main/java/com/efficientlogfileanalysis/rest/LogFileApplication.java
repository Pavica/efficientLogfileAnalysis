package com.efficientlogfileanalysis.rest;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@WebListener
@ApplicationPath("/api")
public class LogFileApplication extends Application implements ServletContextListener {

    public void contextInitialized(ServletContextEvent e) {
        System.out.println("Server started");
    }

    public void contextDestroyed(ServletContextEvent e) {
        System.out.println("Server stopped");
    }
}