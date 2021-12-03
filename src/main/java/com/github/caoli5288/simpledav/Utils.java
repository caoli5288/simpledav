package com.github.caoli5288.simpledav;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static final SimpleDateFormat GMT_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);

    public static String asGmt(Date date) {
        return GMT_DATE_FORMAT.format(date);
    }
}
