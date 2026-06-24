package com.example.uithub.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TuitionResponse {
    @SerializedName("success")
    private boolean success;
    @SerializedName("cached")
    private boolean cached;
    @SerializedName("student")
    private TuitionStudent student;
    @SerializedName("summary")
    private TuitionSummary summary;
    @SerializedName("semesters")
    private List<TuitionItem> semesters;

    public boolean isSuccess() { return success; }
    public boolean isCached() { return cached; }
    public TuitionStudent getStudent() { return student; }
    public TuitionSummary getSummary() { return summary; }
    public List<TuitionItem> getSemesters() { return semesters; }

    public static class TuitionStudent {
        @SerializedName("name")
        private String name;
        @SerializedName("student_id")
        private String studentId;

        public String getName() { return name; }
        public String getStudentId() { return studentId; }
    }

    public static class TuitionSummary {
        @SerializedName("total_due")
        private int totalDue;
        @SerializedName("paid")
        private int paid;
        @SerializedName("remaining")
        private int remaining;

        public int getTotalDue() { return totalDue; }
        public int getPaid() { return paid; }
        public int getRemaining() { return remaining; }
    }
}