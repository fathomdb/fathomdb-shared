package com.fathomdb.extensions;

import com.fathomdb.Configuration;
import com.fathomdb.ConfigurationListener;

public class ObjectBuilder {
    public static <T> T newInstance(Class<T> clazz, Configuration config) {
        T t;
        try {
            t = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Unable to construct instance of class: " + clazz, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to construct instance of class: " + clazz, e);
        }

        if (t instanceof ConfigurationListener) {
            ((ConfigurationListener) t).setConfiguration(config);
        }

        return t;
    }
}
