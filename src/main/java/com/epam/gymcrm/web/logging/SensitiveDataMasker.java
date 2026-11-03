package com.epam.gymcrm.web.logging;

import java.util.regex.Pattern;

/** Hides passwords in JSON bodies and query strings before they are written to the log. */
final class SensitiveDataMasker {

    private static final String MASK = "***";
    private static final Pattern JSON_PASSWORD =
            Pattern.compile("(\"\\w*[Pp]assword\"\\s*:\\s*)\"(?:\\\\.|[^\"\\\\])*\"");
    private static final Pattern QUERY_PASSWORD = Pattern.compile("((?:^|[?&])\\w*[Pp]assword=)[^&]*");

    private SensitiveDataMasker() {
    }

    static String maskJson(String json) {
        return JSON_PASSWORD.matcher(json).replaceAll("$1\"" + MASK + "\"");
    }

    static String maskQuery(String query) {
        return QUERY_PASSWORD.matcher(query).replaceAll("$1" + MASK);
    }
}
