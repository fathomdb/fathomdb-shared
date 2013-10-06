package com.fathomdb.jpa.impl;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;

public class ResultSetMappers {
    final DatabaseNameMapping databaseNameMapping;

    final Map<String, ResultSetMapper> sqlMappers = Maps.newHashMap();
    final Map<String, Class<?>> classes = Maps.newHashMap();

    public ResultSetMappers(DatabaseNameMapping databaseMapping, Class<?>... modelClasses) {
        super();
        this.databaseNameMapping = databaseMapping;
        addClass(modelClasses);
    }

    public void addClass(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            String tableName = databaseNameMapping.getTableName(clazz);
            this.classes.put(tableName, clazz);
        }
    }

    public ResultSetMapper get(String sql) {
        ResultSetMapper resultSetMapper = sqlMappers.get(sql);
        if (resultSetMapper == null) {
            resultSetMapper = new ResultSetMapper(this);
            sqlMappers.put(sql, resultSetMapper);
        }
        return resultSetMapper;
    }

    public Class<?> getClassForTableName(String tableName) {
        return classes.get(tableName);
    }

    public DatabaseNameMapping getNameMapping() {
        return this.databaseNameMapping;
    }

    final LoadingCache<Class<?>, Relationships> relationshipsByClass = CacheBuilder.newBuilder().build(
            new CacheLoader<Class<?>, Relationships>() {
                @Override
                public Relationships load(Class<?> sourceClass) throws Exception {
                    return buildRelationships(sourceClass);
                }
            });

    public void populateInternalRelationships(JoinedQueryResult mapResult) throws SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        for (Class<?> resultClass : mapResult.types.keySet()) {
            Relationships relationships;
            try {
                relationships = relationshipsByClass.get(resultClass);
            } catch (ExecutionException e) {
                throw new IllegalStateException("Error building JPA relationship", e);
            }
            relationships.doMap(mapResult);
        }
    }

    protected Relationships buildRelationships(Class<?> sourceClass) {
        Relationships relationships = new Relationships(sourceClass);

        return relationships;
    }
}
