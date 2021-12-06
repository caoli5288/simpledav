package com.github.caoli5288.simpledav.fs;

import com.github.caoli5288.simpledav.Constants;
import com.github.caoli5288.simpledav.Utils;
import lombok.SneakyThrows;

import java.util.Date;
import java.util.List;

public class FileNode {

    private String filename;
    private long size;
    private Date modified;
    private FileType type;

    @Override
    @SneakyThrows
    public String toString() {
        switch (type) {
            case FILE:
                return Constants.XML_MULTI_STATUS_FILE.format(Utils.concat(Utils.encodeUrl(filename),
                        String.valueOf(size),
                        Utils.asGmt(modified)));
            case DIR:
                return Constants.XML_MULTI_STATUS_DIR.format(Utils.concat(Utils.encodeUrl(filename) + "/",// split strip the end '/' so we needs to append one
                        Utils.asGmt(modified)));
        }
        return null;
    }

    public FileNode filename(String filename) {
        this.filename = filename;
        return this;
    }

    public FileNode size(long size) {
        this.size = size;
        return this;
    }

    public FileNode modified(Date modified) {
        this.modified = modified;
        return this;
    }

    public static FileNode of(FileType type) {
        FileNode node = new FileNode();
        node.type = type;
        return node;
    }

    public static String toString(List<FileNode> nodes) {
        StringBuilder sb = new StringBuilder();
        for (FileNode node : nodes) {
            sb.append(node.toString());
        }
        return sb.toString();
    }
}
