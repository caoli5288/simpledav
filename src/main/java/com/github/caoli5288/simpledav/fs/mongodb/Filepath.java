package com.github.caoli5288.simpledav.fs.mongodb;

import lombok.Value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class Filepath {

    public static final Pattern LIST_PATTERN = Pattern.compile("/([^/]*)/(.*)");

    String bucket;
    String filename;

    public static Filepath extract(String fullUrl) {
        Matcher matcher = LIST_PATTERN.matcher(fullUrl);
        if (matcher.matches()) {
            return new Filepath(matcher.group(1), matcher.group(2));
        }
        return null;
    }
}
