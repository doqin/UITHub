package com.example.uithub.utils;
import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
public class CalendarUtils {
    public static void addEvent(Context context, String title, String location, long beginTime, long endTime) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, title);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);
        context.startActivity(intent);
    }
}