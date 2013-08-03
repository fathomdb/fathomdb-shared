package com.fathomdb.discovery;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

public class ServiceLoaderDiscovery extends Discovery {

    @Override
    public <T> DiscoveredSubTypes<T> getSubTypesOf(Class<T> clazz) {
        final ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);

        return new DiscoveredSubTypes<T>() {

            @Override
            public Iterable<T> getInstances() {
                return serviceLoader;
            }
        };
    }

    @Override
    public List<Class> findClassesInPackage(Package package1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Annotation> Collection<Class> findAnnotatedClasses(Class<T> class1) {
        throw new UnsupportedOperationException();
    }

}
