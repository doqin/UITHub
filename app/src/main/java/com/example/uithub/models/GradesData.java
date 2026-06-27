package com.example.uithub.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GradesData {
    @SerializedName("student_profile")
    private StudentProfile studentProfile;

    @SerializedName("semesters")
    private List<GradeSemester> semesters;

    @SerializedName("summary")
    private GradesSummary summary;

    public StudentProfile getStudentProfile() { return studentProfile; }
    public List<GradeSemester> getSemesters() { return semesters; }
    public GradesSummary getSummary() { return summary; }

    public void setSummary(GradesSummary summary) { this.summary = summary; }
}