package com.github.caoli5288.simpledav.fs;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface IFileSystem {

    void setup();

    List<FileNode> ls(String path) throws IOException;

    InputStream cat(String path) throws IOException;

    void rm(String path);

    void put(String path, InputStream buf);

    void mkdir(String path);

    void mv(String source, String des, boolean force) throws IOException;
}
