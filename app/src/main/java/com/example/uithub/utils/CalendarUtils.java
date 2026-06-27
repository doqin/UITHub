package com.example.uithub.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarUtils {

    private static final String TAG = "CalendarUtils";
    private static final String TIMEZONE = "Asia/Ho_Chi_Minh";

    // Cache calendar ID to avoid repeated queries
    private static long cachedCalendarId = -1;
    private static boolean calendarIdCached = false;

    /**
     * Ánh xạ tên ngày tiếng Việt sang giá trị RFC 5545 BYDAY.
     */
    public static String toByDay(String vietnameseDay) {
        switch (vietnameseDay) {
            case "Thứ 2":
                return "MO";
            case "Thứ 3":
                return "TU";
            case "Thứ 4":
                return "WE";
            case "Thứ 5":
                return "TH";
            case "Thứ 6":
                return "FR";
            case "Thứ 7":
                return "SA";
            default:
                return "";
        }
    }

    /**
     * Kiểm tra xem phòng có chứa "cách 2 tuần" (học 2 tuần/lần).
     */
    public static boolean isBiWeekly(String room) {
        return room != null && room.toLowerCase(Locale.ROOT).contains("cách 2 tuần");
    }

    /**
     * Tạo chuỗi RRULE.
     *
     * @param vietnameseDay ví dụ: "Thứ 3"
     * @param endDateStr    ví dụ: "2024-12-30"
     * @param biWeekly      true nếu học 2 tuần/lần
     */
    public static String buildRRule(String vietnameseDay, String endDateStr, boolean biWeekly) {
        String byDay = toByDay(vietnameseDay);
        if (byDay.isEmpty()) return null;

        // Format UNTIL: YYYYMMDDThhmmssZ (must be in UTC)
        String untilFormatted = "";
        try {
            // Try primary format yyyy-MM-dd, fallback to dd/MM/yy
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
            Date endDate = null;
            try {
                endDate = sdf.parse(endDateStr);
            } catch (Exception e) {
                // Try alternate format dd/MM/yy
                SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yy", Locale.US);
                sdf2.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
                endDate = sdf2.parse(endDateStr);
            }
            if (endDate == null) {
                Log.e(TAG, "Error parsing endDate for RRULE: endDateStr=" + endDateStr);
                return null;
            }
            // Convert to UTC midnight for RRULE UNTIL
            SimpleDateFormat untilFormat = new SimpleDateFormat("yyyyMMdd'T'235959'Z'", Locale.US);
            untilFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            untilFormatted = untilFormat.format(endDate);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing endDate for RRULE: endDateStr=" + endDateStr, e);
            return null;
        }

        if (biWeekly) {
            return "FREQ=WEEKLY;INTERVAL=2;BYDAY=" + byDay + ";UNTIL=" + untilFormatted;
        } else {
            return "FREQ=WEEKLY;BYDAY=" + byDay + ";UNTIL=" + untilFormatted;
        }
    }

    /**
     * Liệt kê tất cả lịch có sẵn để debug.
     */
    private static void dumpAllCalendars(Context context) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = cr.query(
                    CalendarContract.Calendars.CONTENT_URI,
                    new String[]{
                            CalendarContract.Calendars._ID,
                            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                            CalendarContract.Calendars.ACCOUNT_NAME,
                            CalendarContract.Calendars.ACCOUNT_TYPE,
                            CalendarContract.Calendars.VISIBLE,
                            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                            CalendarContract.Calendars.OWNER_ACCOUNT,
                            CalendarContract.Calendars.CAN_ORGANIZER_RESPOND
                    },
                    null,
                    null,
                    null
            );

            if (cursor == null) {
                Log.d(TAG, "dumpAllCalendars: cursor is NULL");
                return;
            }

            Log.d(TAG, "dumpAllCalendars: count=" + cursor.getCount());
            while (cursor.moveToNext()) {
                Log.d(TAG, "dumpAllCalendars: ID=" + cursor.getLong(0)
                        + " Name=" + cursor.getString(1)
                        + " Account=" + cursor.getString(2)
                        + " Type=" + cursor.getString(3)
                        + " Visible=" + cursor.getInt(4)
                        + " Access=" + cursor.getInt(5)
                        + " Owner=" + cursor.getString(6)
                        + " CanOrganizerRespond=" + cursor.getInt(7));
            }
        } catch (SecurityException e) {
            Log.e(TAG, "dumpAllCalendars: SecurityException - READ_CALENDAR not granted?", e);
        } catch (Exception e) {
            Log.e(TAG, "dumpAllCalendars: Error", e);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Tạo lịch cục bộ trên thiết bị và trả về ID, hoặc -1 nếu thất bại.
     */
    private static long createLocalCalendar(Context context) {
        Log.d(TAG, "createLocalCalendar: attempting to create a local calendar...");
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();

        values.put(CalendarContract.Calendars.ACCOUNT_NAME, "UITHub");
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        values.put(CalendarContract.Calendars.NAME, "UITHub Calendar");
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "UITHub");
        values.put(CalendarContract.Calendars.CALENDAR_COLOR, 0xFF1976D2); // Blue
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, "UITHub");
        values.put(CalendarContract.Calendars.VISIBLE, 1);
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);

        try {
            Uri uri = cr.insert(CalendarContract.Calendars.CONTENT_URI, values);
            if (uri != null) {
                long id = Long.parseLong(uri.getLastPathSegment());
                Log.d(TAG, "createLocalCalendar: created calendar with id=" + id + " uri=" + uri);
                return id;
            } else {
                Log.e(TAG, "createLocalCalendar: insert returned null");
                return -1;
            }
        } catch (SecurityException e) {
            Log.e(TAG, "createLocalCalendar: SecurityException - WRITE_CALENDAR not granted?", e);
            return -1;
        } catch (Exception e) {
            Log.e(TAG, "createLocalCalendar: failed", e);
            return -1;
        }
    }

    /**
     * Lấy ID lịch có thể ghi tốt nhất.
     * Ưu tiên:
     * 1. Tài khoản Gmail (ACCOUNT_TYPE=com.google, kết thúc bằng @gmail.com, hiển thị, quyền >= CONTRIBUTOR)
     * 2. Bất kỳ lịch hiển thị + có thể ghi (ACCESS_LEVEL >= CONTRIBUTOR)
     * 3. Bất kỳ lịch hiển thị
     * 4. Dự phòng: tạo lịch cục bộ
     * Kết quả được cache sau lần truy vấn đầu tiên.
     */
    private static long getCalendarId(Context context) {
        // Use cached value if available
        if (calendarIdCached) {
            Log.d(TAG, "getCalendarId: using cached id=" + cachedCalendarId);
            return cachedCalendarId;
        }

        ContentResolver cr = context.getContentResolver();
        Uri calUri = CalendarContract.Calendars.CONTENT_URI;

        Cursor cursor = null;
        long result = -1;
        try {
            // First try: find a Google/Gmail calendar (owner-level)
            Log.d(TAG, "getCalendarId: trying Gmail calendar (account_type=com.google)...");
            cursor = cr.query(calUri,
                    new String[]{
                            CalendarContract.Calendars._ID,
                            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                            CalendarContract.Calendars.ACCOUNT_NAME,
                            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
                    },
                    CalendarContract.Calendars.ACCOUNT_TYPE + " = ? AND " +
                            CalendarContract.Calendars.VISIBLE + " = 1 AND " +
                            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " >= ?",
                    new String[]{"com.google", String.valueOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR)},
                    CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " DESC"
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    String displayName = cursor.getString(1);
                    String accountName = cursor.getString(2);
                    int accessLevel = cursor.getInt(3);
                    Log.d(TAG, "getCalendarId: Google calendar candidate: ID=" + id +
                            " Name=" + displayName + " Account=" + accountName +
                            " Access=" + accessLevel);

                    // Prefer the primary Gmail account (ends with @gmail.com)
                    if (accountName != null && accountName.endsWith("@gmail.com")) {
                        result = id;
                        Log.d(TAG, "getCalendarId: selected Gmail calendar id=" + result +
                                " for account=" + accountName);
                        break;
                    }
                    // Keep first candidate as fallback
                    if (result == -1) {
                        result = id;
                    }
                }
                cursor.close();
                cursor = null;
            }

            // Second try: any visible + writable calendar
            if (result == -1) {
                Log.d(TAG, "getCalendarId: trying any visible + writable calendar...");
                dumpAllCalendars(context);
                cursor = cr.query(calUri,
                        new String[]{CalendarContract.Calendars._ID},
                        CalendarContract.Calendars.VISIBLE + " = 1 AND " +
                                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " >= ?",
                        new String[]{String.valueOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR)},
                        CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " DESC"
                );
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getLong(0);
                    Log.d(TAG, "getCalendarId: found writable calendar id=" + result);
                }
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            // Third try: any visible calendar
            if (result == -1) {
                Log.d(TAG, "getCalendarId: trying any visible calendar...");
                cursor = cr.query(calUri,
                        new String[]{CalendarContract.Calendars._ID},
                        CalendarContract.Calendars.VISIBLE + " = 1",
                        null, null
                );
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getLong(0);
                    Log.d(TAG, "getCalendarId: found visible calendar id=" + result);
                }
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            // If still not found, try to create a local calendar
            if (result == -1) {
                Log.d(TAG, "getCalendarId: no calendar found, attempting to create one...");
                result = createLocalCalendar(context);
                if (result != -1) {
                    Log.d(TAG, "getCalendarId: created local calendar id=" + result);
                } else {
                    Log.e(TAG, "getCalendarId: failed to create local calendar");
                }
            }

            // Cache the result
            cachedCalendarId = result;
            calendarIdCached = true;
            Log.d(TAG, "getCalendarId: cached result=" + result);
            return result;

        } catch (SecurityException e) {
            Log.e(TAG, "getCalendarId: SecurityException - no calendar permission?", e);
            return -1;
        } catch (Exception e) {
            Log.e(TAG, "getCalendarId: Error querying calendars", e);
            return -1;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Kiểm tra xem sự kiện có cùng title, dtstart, và calendar_id đã tồn tại chưa.
     * Trả về true nếu tìm thấy trùng lặp, false nếu không.
     */
    private static boolean isEventDuplicate(Context context, String title, long dtstart, long calendarId) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        try {
            // Query for events with matching TITLE, DTSTART, and CALENDAR_ID
            // We check with a small tolerance (±1 minute) for DTSTART to account for timezone conversions
            long tolerance = 60 * 1000; // 1 minute in milliseconds
            String selection = CalendarContract.Events.TITLE + " = ? AND " +
                    CalendarContract.Events.CALENDAR_ID + " = ? AND " +
                    CalendarContract.Events.DTSTART + " >= ? AND " +
                    CalendarContract.Events.DTSTART + " <= ?";
            String[] selectionArgs = new String[]{
                    title,
                    String.valueOf(calendarId),
                    String.valueOf(dtstart - tolerance),
                    String.valueOf(dtstart + tolerance)
            };

            cursor = cr.query(
                    CalendarContract.Events.CONTENT_URI,
                    new String[]{CalendarContract.Events._ID},
                    selection,
                    selectionArgs,
                    null
            );

            if (cursor != null && cursor.getCount() > 0) {
                Log.d(TAG, "isEventDuplicate: Found duplicate event for title='" + title +
                        "' dtstart=" + dtstart + " calendarId=" + calendarId);
                return true;
            }

            return false;
        } catch (SecurityException e) {
            Log.e(TAG, "isEventDuplicate: SecurityException - READ_CALENDAR not granted?", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "isEventDuplicate: Error checking duplicate", e);
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Chèn một sự kiện đơn lẻ với RRULE (sự kiện lặp lại).
     * Dùng cho cả đồng bộ đơn lẻ và đồng bộ hàng loạt.
     */
    public static Uri insertEvent(Context context, String title, String location, String description,
                                  long beginTime, long endTime, String rrule) {
        Log.d(TAG, "insertEvent: title=" + title + " begin=" + new Date(beginTime)
                + " end=" + new Date(endTime) + " rrule=" + rrule);

        long calendarId = getCalendarId(context);
        if (calendarId == -1) {
            Log.e(TAG, "No calendar found, falling back to Intent for: " + title);
            // Fallback: use Intent if no calendar available
            addEventWithIntent(context, title, location, description, beginTime, endTime);
            return null;
        }

        // Check for duplicate before inserting
        if (isEventDuplicate(context, title, beginTime, calendarId)) {
            Log.d(TAG, "insertEvent: Skipping duplicate event: " + title);
            return null;
        }

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.EVENT_LOCATION, location);
        values.put(CalendarContract.Events.DTSTART, beginTime);
        values.put(CalendarContract.Events.DTEND, endTime);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TIMEZONE);
        values.put(CalendarContract.Events.GUESTS_CAN_MODIFY, 0);
        values.put(CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS, 0);
        values.put(CalendarContract.Events.GUESTS_CAN_SEE_GUESTS, 0);

        if (rrule != null && !rrule.isEmpty()) {
            values.put(CalendarContract.Events.RRULE, rrule);
            Log.d(TAG, "insertEvent: RRULE=" + rrule);
        }

        try {
            Uri uri = context.getContentResolver().insert(
                    CalendarContract.Events.CONTENT_URI, values);
            Log.d(TAG, "insertEvent: success uri=" + uri + " for title=" + title +
                    " calendarId=" + calendarId);
            return uri;
        } catch (Exception e) {
            Log.e(TAG, "insertEvent: failed to insert event: " + title, e);
            return null;
        }
    }

    /**
     * Result class for batch insert operations.
     */
    public static class BatchInsertResult {
        public int inserted;
        public int duplicates;
        public int failed;

        public BatchInsertResult(int inserted, int duplicates, int failed) {
            this.inserted = inserted;
            this.duplicates = duplicates;
            this.failed = failed;
        }

        public int getTotal() {
            return inserted + duplicates + failed;
        }
    }

    /**
     * Chèn nhiều sự kiện cùng lúc.
     * @return BatchInsertResult containing counts of inserted, duplicates, and failed events
     */
    public static BatchInsertResult insertEventsBatch(Context context, List<EventData> events) {
        Log.d(TAG, "insertEventsBatch: inserting " + events.size() + " events");
        int inserted = 0;
        int duplicates = 0;
        int failed = 0;
        for (int i = 0; i < events.size(); i++) {
            EventData event = events.get(i);
            Log.d(TAG, "insertEventsBatch: [" + i + "/" + events.size() + "] inserting: " + event.title);
            
            // Check for duplicate before inserting
            long calendarId = getCalendarId(context);
            if (calendarId != -1 && isEventDuplicate(context, event.title, event.beginTime, calendarId)) {
                Log.d(TAG, "insertEventsBatch: Skipping duplicate event: " + event.title);
                duplicates++;
            } else {
                Uri result = insertEvent(context, event.title, event.location, event.description,
                        event.beginTime, event.endTime, event.rrule);
                if (result != null) {
                    inserted++;
                } else {
                    failed++;
                }
            }
        }
        Log.d(TAG, "insertEventsBatch: done. inserted=" + inserted + " duplicates=" + duplicates + " failed=" + failed);
        return new BatchInsertResult(inserted, duplicates, failed);
    }

    /**
     * Thêm sự kiện vào lịch với hỗ trợ RRULE (cho mục lịch học/môn học).
     * Tự động phát hiện học 2 tuần/lần từ tên phòng.
     */
    public static void addScheduleEvent(Context context, String title, String location, String description,
                                        String vietnameseDay, String dateStr, String endDateStr,
                                        long beginTime, long endTime) {
        Log.d(TAG, "addScheduleEvent: title=" + title + " day=" + vietnameseDay + " date=" + dateStr
                + " end=" + endDateStr + " beginTime=" + beginTime + " endTime=" + endTime
                + " beginDate=" + new Date(beginTime) + " endDate=" + new Date(endTime));

        boolean biWeekly = isBiWeekly(location);
        String rrule = buildRRule(vietnameseDay, endDateStr, biWeekly);
        Log.d(TAG, "addScheduleEvent: biWeekly=" + biWeekly + " rrule=" + rrule);

        insertEvent(context, title, location, description, beginTime, endTime, rrule);
    }

    /**
     * Thêm một sự kiện không lặp lại (cho lịch thi).
     */
    public static void addExamEvent(Context context, String title, String location, String description,
                                    long beginTime, long endTime) {
        Log.d(TAG, "addExamEvent: title=" + title + " begin=" + new Date(beginTime)
                + " end=" + new Date(endTime));
        insertEvent(context, title, location, description, beginTime, endTime, null);
    }

    /**
     * Dự phòng: sử dụng Intent.ACTION_INSERT (hiển thị hộp thoại xác nhận).
     */
    public static void addEventWithIntent(Context context, String title, String location,
                                          String description, long beginTime, long endTime) {
        Log.d(TAG, "addEventWithIntent: title=" + title + " begin=" + new Date(beginTime)
                + " end=" + new Date(endTime));
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, title);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);
        context.startActivity(intent);
    }

    /**
     * Phương thức hỗ trợ chuyển đổi chuỗi ngày+giờ sang millis
     */
    public static long convertToMillis(String date, String time) {
        Log.d(TAG, "convertToMillis: date='" + date + "' time='" + time + "'");
        if (date == null || date.isEmpty() || time == null || time.isEmpty()) {
            Log.w(TAG, "convertToMillis: date or time is null/empty, returning current time");
            return System.currentTimeMillis();
        }

        // Try primary format yyyy-MM-dd, fallback to dd/MM/yy
        String[] patterns = {"yyyy-MM-dd HH:mm", "dd/MM/yy HH:mm"};
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
                String dateTimeStr = date + " " + time;
                Date d = sdf.parse(dateTimeStr);
                if (d != null) {
                    long result = d.getTime();
                    Log.d(TAG, "convertToMillis: parsed '" + dateTimeStr + "' with pattern '" + pattern + "' -> " + result + " (" + d + ")");
                    return result;
                }
            } catch (Exception ignored) {
            }
        }

        Log.e(TAG, "convertToMillis: error parsing date='" + date + "' time='" + time + "' with all patterns");
        return System.currentTimeMillis();
    }

    /**
     * Lớp dữ liệu cho chèn sự kiện hàng loạt.
     */
    public static class EventData {
        public String title;
        public String location;
        public String description;
        public long beginTime;
        public long endTime;
        public String rrule;

        public EventData(String title, String location, String description,
                         long beginTime, long endTime, String rrule) {
            this.title = title;
            this.location = location;
            this.description = description;
            this.beginTime = beginTime;
            this.endTime = endTime;
            this.rrule = rrule;
        }
    }
}