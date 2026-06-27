package com.example.uithub.models;

public class GradesResponse {
    private boolean success;
    private GradesData data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public GradesData getData() {
        return data;
    }

    public void setData(GradesData data) {
        this.data = data;
    }
}
