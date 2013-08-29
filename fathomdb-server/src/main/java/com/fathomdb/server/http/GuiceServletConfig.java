package com.fathomdb.server.http;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class GuiceServletConfig extends GuiceServletContextListener {
    @Inject
    Injector injector;

    private final List<ServletContextListener> listeners = Lists.newArrayList();

    @Override
    protected Injector getInjector() {
        return injector;
    }

    public void addServletContextListener(ServletContextListener listener) {
        listeners.add(listener);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        for (ServletContextListener listener : listeners) {
            listener.contextInitialized(sce);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        for (ServletContextListener listener : listeners) {
            listener.contextDestroyed(sce);
        }
    }
}
