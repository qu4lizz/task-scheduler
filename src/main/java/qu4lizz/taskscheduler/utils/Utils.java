package qu4lizz.taskscheduler.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Utils {
    public final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH.mm.ss");
    public static String getCurrentDateAndTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DATE_TIME_FORMATTER);
    }

    public static String calculateEndDate(String startDate, long seconds) {
        LocalDateTime now = LocalDateTime.parse(startDate, DATE_TIME_FORMATTER);
        now = now.plusSeconds(seconds);
        return now.format(DATE_TIME_FORMATTER);
    }

    public static long dateDifferenceInSeconds(String startDate, String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate, DATE_TIME_FORMATTER);
        LocalDateTime end = LocalDateTime.parse(endDate, DATE_TIME_FORMATTER);
        return end.toEpochSecond(ZoneOffset.ofHours(0)) - start.toEpochSecond(ZoneOffset.ofHours(0));
    }
}
