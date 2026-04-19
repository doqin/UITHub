package com.example.uithub.utils;

import com.example.uithub.models.Announcement;
import com.example.uithub.models.ScheduleItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JSONParser {
    public static List<ScheduleItem> parseSchedule(String json) throws Exception {
        List<ScheduleItem> list = new ArrayList<>();

        JSONObject obj = new JSONObject(json);
        JSONArray arr = obj.getJSONArray("data");

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);

            ScheduleItem item = new ScheduleItem();
            item.day = o.getString("day");
            item.period = o.getString("period");
            item.time = o.getString("time");
            item.start_time = o.getString("start_time");
            item.end_time = o.getString("end_time");
            item.start_date = o.getString("start_date");
            item.end_date = o.getString("end_date");
            item.code = o.getString("code");
            item.name = o.getString("name");
            item.room = o.getString("room");
            item.teacher = o.getString("teacher");
            item.date = o.getString("date");

            list.add(item);
        }

        return list;
    }

    public static List<Announcement> parseAnnouncements(String json) throws Exception {
        List<Announcement> list = new ArrayList<>();

        JSONObject obj = new JSONObject(json);
        JSONArray arr = obj.getJSONArray("data");

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);

            Announcement a = new Announcement();
            a.id = o.getString("_id");
            a.date = o.getString("date");
            a.link = o.getString("link");
            a.node_id = o.getString("node_id");
            a.preview = o.getString("preview");
            a.source = o.getString("source");
            a.title = o.getString("title");
            a.topic = o.getString("topic");
            a.updated_at = o.getString("updated_at");

            list.add(a);
        }

        return list;
    }

    public static Map<String, List<ScheduleItem>> groupByDay(List<ScheduleItem> list) {

        Map<String, List<ScheduleItem>> map = new LinkedHashMap<>();
        for (ScheduleItem item : list) {
            if (!map.containsKey(item.day)) {
                map.put(item.day, new ArrayList<>());
            }
            map.get(item.day).add(item);
        }

        List<String> dayOrder = List.of("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7");

        Map<String, List<ScheduleItem>> sortedMap = new LinkedHashMap<>();
        for (String day : dayOrder) {
            if (map.containsKey(day)) {
                sortedMap.put(day, map.get(day));
            }
        }

        return sortedMap;
    }
}