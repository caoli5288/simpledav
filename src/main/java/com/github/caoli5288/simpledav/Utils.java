package com.github.caoli5288.simpledav;

import lombok.SneakyThrows;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

public class Utils {

    private static final SimpleDateFormat GMT_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);

    public static String asGmt(Date date) {
        return GMT_DATE_FORMAT.format(date);
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static void let(String s, Consumer<String> consumer) {
        if (Utils.isNullOrEmpty(s)) {
            return;
        }
        consumer.accept(s);
    }

    @SneakyThrows
    public static URL asUrl(String s) {
        return new File(s).toURI().toURL();
    }
}
