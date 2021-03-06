package com.fathomdb.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

@Deprecated
// Prefer guava
public interface ByteSource extends Closeable {
    InputStream open() throws IOException;

    long getContentLength() throws IOException;

    ByteMetadata getMetadata();
}
