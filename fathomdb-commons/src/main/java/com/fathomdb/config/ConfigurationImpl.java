package com.fathomdb.config;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Configuration;
import com.fathomdb.properties.PropertyUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ConfigurationImpl extends ConfigurationBase {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationImpl.class);

    final File basePath;
    final List<Map<String, String>> properties;

    public ConfigurationImpl(File basePath, List<Map<String, String>> properties) {
        this.basePath = basePath;
        this.properties = properties;
    }

    @Override
    public String lookup(String key, String defaultValue) {
        for (Map<String, String> propertyMap : properties) {
            String value = propertyMap.get(key);
            if (value != null) {
                return value;
            }
        }
        return defaultValue;
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
    public Configuration getChildTree(String prefix) {
        Map<String, String> childProperties = getChildProperties(prefix);
        List<Map<String, String>> propertyList = Lists.newArrayList();
        propertyList.add(childProperties);
        return new ConfigurationImpl(basePath, propertyList);
    }

    @Override
    public Set<String> getKeys() {
        Set<String> keys = Sets.newHashSet();

        for (Map<String, String> propertyMap : properties) {
            for (Entry<String, String> entry : propertyMap.entrySet()) {
                String key = entry.getKey();
                if (entry.getValue() == null) {
                    keys.remove(key);
                } else {
                    keys.add(key);
                }
            }
        }

        return keys;
    }

    public static ConfigurationImpl load() {
        return load(null);
    }

    public static ConfigurationImpl load(String configFilePath) {
        if (configFilePath == null) {
            configFilePath = System.getProperty("conf");
        }

        if (configFilePath == null) {
            configFilePath = System.getenv("CONFIGURATION_FILE");
        }

        if (configFilePath == null) {
            configFilePath = new File(new File("."), "configuration.properties").getAbsolutePath();
        }

        File configFile = new File(configFilePath);

        List<Map<String, String>> propertiesList = Lists.newArrayList();

        {
            Properties envVariables = new Properties();
            envVariables.putAll(System.getenv());
            propertiesList.add(PropertyUtils.toMap(envVariables));
        }

        {

            if (configFile.exists()) {
                try {
                    Properties properties = new Properties();
                    PropertyUtils.loadProperties(properties, configFile);
                    propertiesList.add(PropertyUtils.toMap(properties));
                    log.info("Loaded configuration file: " + configFile);
                } catch (IOException e) {
                    throw new IllegalStateException("Error loading configuration file: " + configFile, e);
                }
            } else {
                log.warn("Configuration file not found: " + configFile);
            }
        }

        {
            Properties systemProperties = System.getProperties();

            Map<String, String> confProperties = PropertyUtils.getChildProperties(
                    PropertyUtils.toMap(systemProperties), "conf.");
            if (!confProperties.isEmpty()) {
                propertiesList.add(confProperties);
            }
        }

        propertiesList = Lists.reverse(propertiesList);

        return new ConfigurationImpl(configFile.getParentFile(), propertiesList);
    }

    public static ConfigurationImpl from(File basePath, List<Map<String, String>> propertiesList) {
        return new ConfigurationImpl(basePath, propertiesList);
    }

    public static ConfigurationImpl from(File basePath, Properties properties) {
        return new ConfigurationImpl(basePath, Collections.singletonList(PropertyUtils.toMap(properties)));
    }

    @Override
    public File getBasePath() {
        return basePath;
    }

}
