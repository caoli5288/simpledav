package com.github.caoli5288.simpledav.fs;

import com.github.caoli5288.simpledav.Constants;
import com.github.caoli5288.simpledav.Utils;
import lombok.Value;

import java.util.Date;
import java.util.List;

@Value
public class FileNode {

    String filename;
    long size;
    Date modified;
    FileType type;

    @Override
    public String toString() {
        switch (type) {
            case FILE:
                return Constants.XML_MULTI_STATUS_FILE.format(Utils.concat(filename,
                        String.valueOf(size),
                        Utils.asGmt(modified)));
            case DIR:
                return Constants.XML_MULTI_STATUS_DIR.format(Utils.concat(filename,
                        Utils.asGmt(modified)));
        }
        return null;
    }

    public static String toString(List<FileNode> nodes) {
        StringBuilder sb = new StringBuilder();
        for (FileNode node : nodes) {
            sb.append(node.toString());
        }
        return sb.toString();
    }
}
