package com.example.uithub.models;

import java.util.List;

public class Announcement {

    public String _id;
    public String node_id;
    public String title;
    public String preview;
    public String date;
    public String link;
    public String source;
    public String topic;
    public String updated_at;

    private Details details;

    public static class Details {

        private String content;
        private List<Related> related;

        public String getContent() {
            return content;
        }

        public List<Related> getRelated() {
            return related;
        }
    }

    public static class Related {

        private String title;
        private String link;

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }
    }

    public String get_id() {
        return _id;
    }

    public String getNode_id() {
        return node_id;
    }

    public String getTitle() {
        return title;
    }

    public String getPreview() {
        return preview;
    }

    public String getDate() {
        return date;
    }

    public Details getDetails() {
        return details;
    }
}