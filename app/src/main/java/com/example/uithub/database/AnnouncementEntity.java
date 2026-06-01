package com.example.uithub.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ANNOUNCEMENT")
public class AnnouncementEntity {
    @PrimaryKey
    @NonNull
    String url;
    String headline;
    String content;
    String postedTime;

    public AnnouncementEntity(@NonNull String url, String headline, String content, String postedTime) {
        this.url = url;
        this.headline = headline;
        this.content = content;
        this.postedTime = postedTime;
    }

    @NonNull
    public String getUrl() { return url; }
    public void setUrl(@NonNull String url) { this.url = url; }

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPostedTime() { return postedTime; }
    public void setPostedTime(String postedTime) { this.postedTime = postedTime; }
}
