package com.example.uithub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AnnouncementParser {
    static public AnnouncementArticle[] parse() throws IOException {
        Document doc = Jsoup.connect("https://student.uit.edu.vn/thong-bao-chung").get();
        System.out.println(doc.title());
        Elements articles = doc.getElementsByTag("article");
        ArrayList<AnnouncementArticle> articleList = new ArrayList<>();
        for (Element article : articles) {
            Element header = article.getElementsByTag("h2").first();
            if (header == null)
                continue; // for some reason, there are articles from long ago in recent pages
            Element a = header.getElementsByTag("a").first();
            assert a != null;
            String title = header.text();
            String url = Objects.requireNonNull(a.attribute("href")).getValue();
            Element content = article.getElementsByClass("content").first();
            if (content == null) continue;
            String contentText = content.text();
            Element submitted = article.getElementsByClass("submitted").first();
            if (submitted == null) continue;
            Element dateSpan = submitted.getElementsByTag("span").first();
            assert dateSpan != null;
            String dateString = Objects.requireNonNull(dateSpan.attribute("content")).getValue();
            LocalDateTime dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
            articleList.add(new AnnouncementArticle(title, contentText, dateTime, url));
        }
        return articleList.toArray(new AnnouncementArticle[0]);
    }
}
