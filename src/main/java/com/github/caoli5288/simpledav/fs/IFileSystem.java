package com.github.caoli5288.simpledav.fs;

import java.io.InputStream;
import java.util.List;

public interface IFileSystem {

    void setup();

    List<FileNode> ls(String fullUrl);

    InputStream cat(String fullUrl);

    void rm(String fullUrl);

    void put(String fullUrl, InputStream buf);

    void mkdir(String fullUrl);
}
