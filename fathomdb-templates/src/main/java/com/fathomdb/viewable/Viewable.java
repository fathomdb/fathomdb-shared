package com.fathomdb.viewable;

public class Viewable {

    private final String path;
    private final Object model;

    public Viewable(String path, Object model) {
        this.path = path;
        this.model = model;
    }

    public Object getModel() {
        return model;
    }

    public String getPath() {
        return path;
    }

}
