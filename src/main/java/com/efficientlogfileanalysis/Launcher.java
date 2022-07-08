package com.efficientlogfileanalysis;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class Launcher implements ServletContextListener {

    public void contextInitialized(ServletContextEvent e) {
        System.out.println("Server started");
    }

    public void contextDestroyed(ServletContextEvent e) {
        System.out.println("Server stopped");
    }
}
