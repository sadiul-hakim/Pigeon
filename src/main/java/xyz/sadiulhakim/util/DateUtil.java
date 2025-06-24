package xyz.sadiulhakim.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    private DateUtil() {
    }

    private static final DateTimeFormatter DEFAULT_FORMATER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MESSAGE_FORMATER = DateTimeFormatter.ofPattern("MMM dd, HH:mm a");

    public static String format(LocalDateTime dateTime) {
        return DEFAULT_FORMATER.format(dateTime);
    }

    public static String format(LocalDateTime dateTime, String format) {
        DateTimeFormatter FORMATER = DateTimeFormatter.ofPattern(format);
        return FORMATER.format(dateTime);
    }

    public static String getLastSeenTime(LocalDateTime lastSeen) {
        if (lastSeen == null) return "Never seen";
        Duration duration = Duration.between(lastSeen, LocalDateTime.now());

        long seconds = duration.getSeconds();
        if (seconds < 60) return "Active " + seconds + " seconds ago";
        if (seconds < 3600) return "Active " + (seconds / 60) + " minutes ago";
        if (seconds < 86400) return "Active " + (seconds / 3600) + " hours ago";
        if (seconds < 172800) return "Active yesterday";
        return "Active " + (seconds / 86400) + " days ago";
    }

    public static String formatMessageDate(LocalDateTime dateTime) {
        return MESSAGE_FORMATER.format(dateTime);
    }
}
