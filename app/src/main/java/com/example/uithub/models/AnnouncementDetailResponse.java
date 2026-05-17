package com.example.uithub.models;

public class AnnouncementDetailResponse {

    private boolean success;
    private Announcement data;

    public boolean isSuccess() {
        return success;
    }

    public Announcement getData() {
        return data;
    }
}