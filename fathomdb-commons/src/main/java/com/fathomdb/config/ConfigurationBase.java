package com.fathomdb.config;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Configuration;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.net.InetAddresses;

public abstract class ConfigurationBase implements Configuration {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationBase.class);

    @Override
    public int lookup(String key, int defaultValue) {
        String s = lookup(key, "" + defaultValue);
        return Integer.parseInt(s);
    }

    @Override
    public String get(String key) {
        String value = find(key);
        if (value == null) {
            throw new IllegalArgumentException("Required value not found: " + key);
        }
        return value;
    }

    @Override
    public String find(String key) {
        String value = lookup(key, (String) null);
        return value;
    }

    @Override
    public Map<String, String> getChildProperties(String prefix) {
        Map<String, String> children = Maps.newHashMap();

        Set<String> keySet = getKeys();
        for (String key : keySet) {
            if (!key.startsWith(prefix)) {
                continue;
            }

            String suffix = key.substring(prefix.length());
            children.put(suffix, lookup(key, (String) null));
        }

        return children;
    }

    @Override
    public File lookupFile(String key, String defaultPath) {
        String value = lookup(key, defaultPath);
        if (value == null) {
            assert defaultPath == null;
            return null;
        }

        if (value.startsWith("~/")) {
            value = System.getProperty("user.home") + File.separator + value.substring(2);
        }

        if (value.startsWith("/")) {
            return new File(value);
        } else {
            return new File(getBasePath(), value);
        }
    }

    @Override
    public boolean lookup(String key, boolean defaultValue) {
        String s = lookup(key, Boolean.toString(defaultValue));
        return Boolean.parseBoolean(s);
    }

    @Override
    public List<InetSocketAddress> lookupList(String key, InetSocketAddress... defaults) {
        List<InetSocketAddress> ret = Lists.newArrayList();
        String s = find(key);
        if (s == null) {
            ret.addAll(Arrays.asList(defaults));
        } else {
            for (String v : Splitter.on(',').split(s)) {
                InetSocketAddress inetSocketAddress = parseInetSocketAddress(v);
                ret.add(inetSocketAddress);
            }
        }
        return ret;
    }

    public static InetSocketAddress parseInetSocketAddress(String s) {
        int colonIndex = s.lastIndexOf(':');
        if (colonIndex == -1) {
            throw new IllegalArgumentException("Cannot parse address: " + s);
        }

        InetAddress addr = InetAddresses.forString(s.substring(0, colonIndex).trim());
        int port = Integer.valueOf(s.substring(colonIndex + 1));

        return new InetSocketAddress(addr, port);
    }

    @Override
    public InetAddress lookup(String key, InetAddress defaultValue) {
        String value = lookup(key, (String) null);
        if (value == null) {
            return defaultValue;
        }
        return InetAddresses.forString(value);
    }

    @Override
    public <E extends Enum<E>> E lookup(String key, E defaultValue) {
        String value = lookup(key, (String) null);
        if (value == null) {
            return defaultValue;
        }
        Class<Enum<E>> enumClass = (Class<Enum<E>>) defaultValue.getClass();
        for (Enum<E> e : enumClass.getEnumConstants()) {
            if (e.name().equalsIgnoreCase(value)) {
                return (E) e;
            }
        }

        throw new IllegalArgumentException("Invalid value: " + key + "=" + value);
    }

}
