package com.example.uithub.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DeadlineResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("cached")
    private boolean cached;

    @SerializedName("count")
    private int count;

    @SerializedName("data")
    private List<Deadline> data;

    public boolean isSuccess() {
        return success;
    }

    public boolean isCached() {
        return cached;
    }

    public int getCount() {
        return count;
    }

    public List<Deadline> getData() {
        return data;
    }
}
