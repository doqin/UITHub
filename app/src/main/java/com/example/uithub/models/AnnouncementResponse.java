package com.example.uithub.models;

import java.util.List;

public class AnnouncementResponse {

    public boolean success;
    public int count;
    public List<Announcement> data;

    public List<Announcement> getData() {
        return data;
    }
}