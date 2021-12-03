package com.github.caoli5288.simpledav;

import kotlin.io.ByteStreamsKt;

import java.io.InputStream;
import java.util.Objects;

public class Constants {

    public static final String XML_MULTI_STATUS = readResource("multistatus.xml");
    public static final String XML_MULTI_STATUS_FILE = readResource("multistatus-file.xml");
    public static final String XML_MULTI_STATUS_DIR = readResource("multistatus-dir.xml");

    private static String readResource(String s) {
        InputStream stream = Constants.class.getClassLoader().getResourceAsStream(s);
        return new String(ByteStreamsKt.readBytes(Objects.requireNonNull(stream)));
    }
}
