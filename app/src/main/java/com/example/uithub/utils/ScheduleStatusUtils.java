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
            DateRange range = parseDateRange(item);
            LocalDate startDate = range.start;
            LocalDate endDate = range.end;

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

    private static DateRange parseDateRange(ScheduleItem item) {
        String dateStr = item.date;
        if (dateStr != null && dateStr.contains(" -> ")) {
            try {
                String[] parts = dateStr.split(" -> ");
                LocalDate start = LocalDate.parse(parts[0].trim(), SCHEDULE_DATE_FORMATTER);
                LocalDate end = LocalDate.parse(parts[1].trim(), SCHEDULE_DATE_FORMATTER);
                return new DateRange(start, end);
            } catch (DateTimeParseException e) {
                // Fall through to legacy parsing
            }
        }

        try {
            LocalDate start = LocalDate.parse(item.start_date, SCHEDULE_DATE_FORMATTER);
            LocalDate end = LocalDate.parse(item.end_date, SCHEDULE_DATE_FORMATTER);
            return new DateRange(start, end);
        } catch (DateTimeParseException | NullPointerException e) {
            return new DateRange(LocalDate.MIN, LocalDate.MAX);
        }
    }

    private static class DateRange {
        LocalDate start;
        LocalDate end;

        DateRange(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }
    }
}
