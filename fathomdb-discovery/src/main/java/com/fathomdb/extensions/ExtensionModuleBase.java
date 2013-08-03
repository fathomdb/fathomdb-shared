package com.fathomdb.extensions;

import java.util.Collections;
import java.util.List;

import com.fathomdb.Configuration;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

public abstract class ExtensionModuleBase extends AbstractModule implements ExtensionModule {

    @Override
    public void addEntities(List<Class<?>> entities) {

    }

    @Override
    public void addHttpExtensions(HttpConfiguration servletModule) {

    }

    @Override
    public Module getOverrideModule() {
        return null;
    }

    @Override
    public List<Module> getExtraModules(Configuration configuration) {
        return Collections.<Module> singletonList(this);
    }
}
