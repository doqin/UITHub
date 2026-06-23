package com.example.uithub.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.uithub.R;
import com.example.uithub.models.ScheduleItem;
import com.example.uithub.utils.JSONParser;
import com.example.uithub.utils.PreferenceManager;
import com.example.uithub.utils.ScheduleStatusUtils;

import java.util.ArrayList;
import java.util.List;

public class ScheduleWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ScheduleWidgetFactory(this.getApplicationContext(), intent);
    }
}

class ScheduleWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private int appWidgetId;
    private List<ScheduleItem> todayItems = new ArrayList<>();

    public ScheduleWidgetFactory(Context context, Intent intent) {
        this.context = context;
        this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        loadSchedule();
    }

    @Override
    public void onDataSetChanged() {
        loadSchedule();
    }

    private void loadSchedule() {
        todayItems.clear();
        PreferenceManager preferenceManager = new PreferenceManager(context);
        String json = preferenceManager.getScheduleJson();
        if (json != null && !json.isEmpty()) {
            try {
                List<ScheduleItem> allItems = JSONParser.parseSchedule(json);
                for (ScheduleItem item : allItems) {
                    if (ScheduleStatusUtils.isOpenToday(item)) {
                        todayItems.add(item);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        todayItems.clear();
    }

    @Override
    public int getCount() {
        return todayItems.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position >= todayItems.size()) return null;

        ScheduleItem item = todayItems.get(position);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_schedule_item);

        views.setTextViewText(R.id.item_name, item.name);
        
        String timeStr = item.time != null && !item.time.isEmpty() ? item.time : "Tiết " + item.period;
        views.setTextViewText(R.id.item_time, timeStr);
        
        views.setTextViewText(R.id.item_room, "Phòng " + item.room);

        Intent fillInIntent = new Intent();
        views.setOnClickFillInIntent(R.id.item_name, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
