package com.github.caoli5288.simpledav;

import kotlin.io.ByteStreamsKt;

import java.text.MessageFormat;
import java.util.Objects;

public class Constants {

    public static final MessageFormat XML_MULTI_STATUS = new MessageFormat(readResource("multistatus.xml"));
    public static final MessageFormat XML_MULTI_STATUS_FILE = new MessageFormat(readResource("multistatus-file.xml"));
    public static final MessageFormat XML_MULTI_STATUS_DIR = new MessageFormat(readResource("multistatus-dir.xml"));

    private static String readResource(String s) {
        return new String(ByteStreamsKt.readBytes(Objects.requireNonNull(Constants.class.getClassLoader().getResourceAsStream(s))));
    }
}
