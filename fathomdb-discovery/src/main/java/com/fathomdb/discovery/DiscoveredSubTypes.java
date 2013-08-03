package com.fathomdb.discovery;

public interface DiscoveredSubTypes<T> {

    Iterable<T> getInstances();

}
