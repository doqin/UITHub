package com.example.uithub.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.uithub.R;
import com.example.uithub.models.Deadline;
import com.example.uithub.models.DeadlineResponse;
import com.example.uithub.utils.PreferenceManager;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeadlineWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new DeadlineWidgetFactory(this.getApplicationContext(), intent);
    }
}

class DeadlineWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private int appWidgetId;
    private List<Deadline> deadlines = new ArrayList<>();
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public DeadlineWidgetFactory(Context context, Intent intent) {
        this.context = context;
        this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        loadDeadlines();
    }

    @Override
    public void onDataSetChanged() {
        loadDeadlines();
    }

    private void loadDeadlines() {
        deadlines.clear();
        PreferenceManager preferenceManager = new PreferenceManager(context);
        String json = preferenceManager.getDeadlinesJson();
        if (json != null && !json.isEmpty()) {
            try {
                DeadlineResponse response = new Gson().fromJson(json, DeadlineResponse.class);
                if (response != null && response.getData() != null) {
                    deadlines.addAll(response.getData());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        deadlines.clear();
    }

    @Override
    public int getCount() {
        return deadlines.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position >= deadlines.size()) return null;

        Deadline item = deadlines.get(position);
        com.example.uithub.utils.PreferenceManager pref = new com.example.uithub.utils.PreferenceManager(context);
        int layoutId = pref.isDarkMode() ? R.layout.widget_deadline_item_dark : R.layout.widget_deadline_item;
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);

        views.setTextViewText(R.id.item_title, item.getTitle() != null ? item.getTitle() : "");
        views.setTextViewText(R.id.item_course, item.getCourseCode() != null ? item.getCourseCode() : "");

        String endText = "";
        if (item.getEnd() != null && !item.getEnd().isEmpty()) {
            try {
                Date date = inputFormat.parse(item.getEnd());
                endText = outputFormat.format(date);
            } catch (ParseException e) {
                endText = item.getEnd();
            }
        }
        views.setTextViewText(R.id.item_end, endText);

        Intent fillInIntent = new Intent();
        views.setOnClickFillInIntent(R.id.item_title, fillInIntent);

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
