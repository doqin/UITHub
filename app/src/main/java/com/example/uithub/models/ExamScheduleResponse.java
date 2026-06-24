package com.example.uithub.models;

import java.util.List;

public class ExamScheduleResponse {
    private boolean success;
    private boolean cached;
    private int count;
    private List<ExamModel> data;

    public boolean isSuccess() { return success; }
    public boolean isCached() { return cached; }
    public int getCount() { return count; }
    public List<ExamModel> getData() { return data; }
}