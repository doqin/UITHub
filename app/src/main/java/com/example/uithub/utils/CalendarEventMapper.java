package com.example.uithub.utils;

import com.example.uithub.models.ExamModel;
import com.example.uithub.models.ScheduleItem;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

public class CalendarEventMapper {
    public static Event fromExam(ExamModel exam) {
        Event event = new Event()
                .setSummary("Thi: " + exam.getCourse_code())
                .setLocation(exam.getRoom())
                .setDescription("Ca thi: " + exam.getExam_shift());
        String startStr = exam.getExam_date() + "T" + exam.getStart_time() + ":00Z";
        String endStr = exam.getExam_date() + "T" + exam.getEnd_time() + ":00Z";
        event.setStart(new EventDateTime().setDateTime(new DateTime(startStr)));
        event.setEnd(new EventDateTime().setDateTime(new DateTime(endStr)));
        return event;
    }
    public static Event fromSchedule(ScheduleItem item) {
        Event event = new Event()
                .setSummary(item.name)
                .setLocation(item.room)
                .setDescription("GV: " + item.teacher);

        String startStr = item.date + "T" + item.start_time + ":00Z";
        String endStr = item.date + "T" + item.end_time + ":00Z";

        event.setStart(new EventDateTime().setDateTime(new DateTime(startStr)));
        event.setEnd(new EventDateTime().setDateTime(new DateTime(endStr)));
        return event;
    }
}