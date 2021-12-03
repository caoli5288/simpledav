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
        if (type == FileType.DIR) {
            return Constants.XML_MULTI_STATUS_DIR
                    .replace("%path%", filename)
                    .replace("%date%", Utils.asGmt(modified));
        }
        return Constants.XML_MULTI_STATUS_FILE
                .replace("%path%", filename)
                .replace("%size%", String.valueOf(size))
                .replace("%date%", Utils.asGmt(modified));
    }

    public static String toString(List<FileNode> nodes) {
        StringBuilder sb = new StringBuilder();
        for (FileNode node : nodes) {
            sb.append(node.toString());
        }
        return sb.toString();
    }
}
