package com.fathomdb;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Configuration {
    String get(String key);

    String lookup(String key, String defaultValue);

    boolean lookup(String key, boolean defaultValue);

    int lookup(String key, int defaultValue);

    String find(String key);

    File getBasePath();

    File lookupFile(String key, String defaultValue);

    Map<String, String> getChildProperties(String prefix);

    Configuration getChildTree(String prefix);

    Set<String> getKeys();

    List<InetSocketAddress> lookupList(String key, InetSocketAddress... inetSocketAddress);

    InetAddress lookup(String key, InetAddress defaultValue);

    <E extends Enum<E>> E lookup(String key, E defaultValue);
}
