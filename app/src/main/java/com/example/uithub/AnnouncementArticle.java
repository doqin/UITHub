package com.example.uithub;

import org.jsoup.nodes.Element;

import java.time.LocalDateTime;

public class AnnouncementArticle {
    String title;
    String content;
    String url;
    LocalDateTime submitted;

    public AnnouncementArticle(String title, String content, LocalDateTime dateTime, String url) {
        this.title = title;
        this.content = content;
        this.submitted = dateTime;
        this.url = url;
    }
}
