package com.example.uithub.models;

import com.google.gson.annotations.SerializedName;

public class ExamModel {
    @SerializedName("stt") private int stt;
    @SerializedName("course_code") private String course_code;
    @SerializedName("class_code") private String class_code;
    @SerializedName("exam_date") private String exam_date;
    @SerializedName("exam_datetime") private String exam_datetime;
    @SerializedName("exam_shift") private String exam_shift;
    @SerializedName("start_time") private String start_time;
    @SerializedName("end_time") private String end_time;
    @SerializedName("weekday") private String weekday;
    @SerializedName("room") private String room;
    @SerializedName("status") private String status;
    @SerializedName("days_remaining") private int days_remaining;

    public int getStt() { return stt; }
    public String getCourse_code() { return course_code; }
    public String getClass_code() { return class_code; }
    public String getExam_date() { return exam_date; }
    public String getExam_datetime() { return exam_datetime; }
    public String getExam_shift() { return exam_shift; }
    public String getStart_time() { return start_time; }
    public String getEnd_time() { return end_time; }
    public String getWeekday() { return weekday; }
    public String getRoom() { return room; }
    public String getStatus() { return status; }
    public int getDays_remaining() { return days_remaining; }
}