package com.example.uithub.utils;

import com.example.uithub.models.ScheduleItem;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class ScheduleStatusUtils {
    private static final DateTimeFormatter SCHEDULE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy");

    private ScheduleStatusUtils() {
    }

    public static boolean isOpenThisWeek(ScheduleItem item) {
        return isOpenOnDate(item, LocalDate.now(), false, true);
    }

    public static boolean isOpenToday(ScheduleItem item) {
        return isOpenOnDate(item, LocalDate.now(), true, false);
    }

    private static boolean isOpenOnDate(ScheduleItem item, LocalDate date, boolean requireMatchingDay, boolean fallback) {
        try {
            LocalDate startDate = LocalDate.parse(item.start_date, SCHEDULE_DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(item.end_date, SCHEDULE_DATE_FORMATTER);

            if (date.isBefore(startDate) || date.isAfter(endDate)) {
                return false;
            }

            if (requireMatchingDay && !dayLabelFor(date.getDayOfWeek()).equals(item.day)) {
                return false;
            }

            if (isBiweeklyClass(item)) {
                long weeksSinceStart = ChronoUnit.WEEKS.between(startDate, date);
                return weeksSinceStart % 2 == 0;
            }

            return true;
        } catch (DateTimeParseException | NullPointerException e) {
            return fallback;
        }
    }

    private static String dayLabelFor(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return "Thứ 2";
            case TUESDAY:
                return "Thứ 3";
            case WEDNESDAY:
                return "Thứ 4";
            case THURSDAY:
                return "Thứ 5";
            case FRIDAY:
                return "Thứ 6";
            case SATURDAY:
                return "Thứ 7";
            default:
                return "Chủ nhật";
        }
    }

    private static boolean isBiweeklyClass(ScheduleItem item) {
        return containsBiweeklyMarker(item.room)
                || containsBiweeklyMarker(item.date)
                || containsBiweeklyMarker(item.time)
                || containsBiweeklyMarker(item.period)
                || containsBiweeklyMarker(item.name);
    }

    private static boolean containsBiweeklyMarker(String value) {
        return value != null && value.contains("Cách 2 tuần");
    }
}
