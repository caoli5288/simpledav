package com.github.caoli5288.simpledav;

import sun.nio.cs.ThreadLocalCoders;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    private static final SimpleDateFormat GMT_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
    private static final Pattern SPACE = Pattern.compile(" ");
    private static final Pattern ILLEGAL_FILENAME_PATTERN = Pattern.compile("[\\\\/:?\"<>|]");

    public static String asGmt(Date date) {
        return GMT_DATE_FORMAT.format(date) + " GMT";
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

    public static Object concat(Object... objects) {
        return objects;
    }

    public static String encodeUrl(String filepath) {
        return Arrays.stream(filepath.split("/"))
                .map(Utils::escapeStr)
                .map(s -> SPACE.matcher(s).replaceAll("%20"))
                .collect(Collectors.joining("/"));
    }

    // Encodes all characters >= \u0080 into escaped, normalized UTF-8 octets,
    // assuming that s is otherwise legal
    //
    public static String escapeStr(String s) {
        int n = s.length();
        if (n == 0)
            return s;

        // First check whether we actually need to encode
        for (int i = 0; ; ) {
            if (s.charAt(i) >= '\u0080')
                break;
            if (++i >= n)
                return s;
        }

        String ns = Normalizer.normalize(s, Normalizer.Form.NFC);
        ByteBuffer bb = null;
        try {
            bb = ThreadLocalCoders.encoderFor("UTF-8")
                    .encode(CharBuffer.wrap(ns));
        } catch (CharacterCodingException x) {
            assert false;
        }

        StringBuffer sb = new StringBuffer();
        while (bb.hasRemaining()) {
            int b = bb.get() & 0xff;
            if (b >= 0x80)
                appendEscape(sb, (byte) b);
            else
                sb.append((char) b);
        }
        return sb.toString();
    }

    // -- Escaping and encoding --

    private final static char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static void appendEscape(StringBuffer sb, byte b) {
        sb.append('%');
        sb.append(HEX_DIGITS[(b >> 4) & 0x0f]);
        sb.append(HEX_DIGITS[(b) & 0x0f]);
    }

    public static void checkFilename(String f) throws FileNotFoundException {
        if (ILLEGAL_FILENAME_PATTERN.matcher(f).find()) {
            throw new FileNotFoundException();
        }
    }
}
