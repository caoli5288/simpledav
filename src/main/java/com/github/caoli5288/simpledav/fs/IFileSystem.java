package com.github.caoli5288.simpledav.fs;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface IFileSystem {

    void setup();

    List<FileNode> ls(String path, boolean lookup) throws IOException;

    InputStream cat(String path) throws IOException;

    void rm(String path);

    void put(String path, InputStream buf) throws IOException;

    void mkdir(String path) throws IOException;

    void mv(String source, String des, boolean force) throws IOException;

    void copy(String from, String dst) throws IOException;
}
