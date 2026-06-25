package com.example.uithub.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Deadline implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("course_code")
    private String courseCode;

    @SerializedName("start")
    private String start;

    @SerializedName("end")
    private String end;

    @SerializedName("url")
    private String url;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getUrl() {
        return url;
    }
}
