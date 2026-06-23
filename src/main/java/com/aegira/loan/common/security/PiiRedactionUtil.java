package com.aegira.loan.common.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PiiRedactionUtil {
    private static final Pattern EMAIL = Pattern.compile("(?i)\\b([a-z0-9._%+-]{1,})@([a-z0-9.-]+\\.[a-z]{2,})\\b");
    private static final Pattern PHONE = Pattern.compile("(?<!\\d)(?:\\+62|62|0)\\d{8,14}(?!\\d)");
    private static final Pattern NIK = Pattern.compile("(?<!\\d)\\d{12,20}(?!\\d)");
    private static final Pattern SECRET = Pattern.compile("(?i)\\b(password|token)\\s*[:=]\\s*[^\\s,;]+" );
    private static final Pattern BEARER = Pattern.compile("(?i)Authorization\\s*:\\s*Bearer\\s+[^\\s,;]+" );

    private PiiRedactionUtil() {
    }

    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        String sanitized = replaceEmails(value);
        sanitized = replacePhones(sanitized);
        sanitized = replaceNik(sanitized);
        sanitized = SECRET.matcher(sanitized).replaceAll("$1=[REDACTED]");
        return BEARER.matcher(sanitized).replaceAll("Authorization: Bearer [REDACTED]");
    }

    private static String replaceEmails(String value) {
        Matcher matcher = EMAIL.matcher(value);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String local = matcher.group(1);
            String visible = local.length() <= 2 ? local : local.substring(0, 2);
            matcher.appendReplacement(output, Matcher.quoteReplacement(visible + "***@" + matcher.group(2)));
        }
        matcher.appendTail(output);
        return output.toString();
    }

    private static String replaceNik(String value) {
        Matcher matcher = NIK.matcher(value);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String number = matcher.group();
            matcher.appendReplacement(output, Matcher.quoteReplacement(number.substring(0, 4) + "****" + number.substring(number.length() - 4)));
        }
        matcher.appendTail(output);
        return output.toString();
    }

    private static String replacePhones(String value) {
        Matcher matcher = PHONE.matcher(value);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String phone = matcher.group();
            String masked = phone.length() <= 8 ? "****" : phone.substring(0, 4) + "****" + phone.substring(phone.length() - 4);
            matcher.appendReplacement(output, Matcher.quoteReplacement(masked));
        }
        matcher.appendTail(output);
        return output.toString();
    }
}
