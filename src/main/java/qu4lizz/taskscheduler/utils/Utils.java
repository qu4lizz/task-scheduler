package qu4lizz.taskscheduler.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Utils {
    public static String getCurrentDateAndTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH.mm.ss");
        LocalDateTime now = LocalDateTime.now();
        return now.format(dtf);
    }

    public static String calculateEndDate(String startDate, int time) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH.mm.ss");
        LocalDateTime now = LocalDateTime.parse(startDate, dtf);
        now = now.plusSeconds(time);
        return now.format(dtf);
    }

    public static int dateDifference(String startDate, String endDate) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH.mm.ss");
        LocalDateTime start = LocalDateTime.parse(startDate, dtf);
        LocalDateTime end = LocalDateTime.parse(endDate, dtf);
        return (int) (end.toEpochSecond(ZoneOffset.ofHours(0)) - start.toEpochSecond(ZoneOffset.ofHours(0)));
    }
}
