package com.example.uithub;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void announcement_parsing() {
        try {
            AnnouncementArticle[] articles = AnnouncementParser.parse();
            for (AnnouncementArticle article : articles) {
                System.out.println(article.title);
                System.out.println(article.content);
                System.out.println(article.submitted);
                System.out.println(article.url);
                System.out.println();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}