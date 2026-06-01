package com.example.uithub;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.uithub.database.AnnouncementDao;
import com.example.uithub.database.AnnouncementEntity;
import com.example.uithub.database.AppDatabase;

import java.io.IOException;

public class AnnouncementWorker extends Worker {
    private static final String CHANNEL_ID = "announcement_channel";

    public AnnouncementWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            AnnouncementArticle[] articles = AnnouncementParser.parse();
            AnnouncementDao dao = AppDatabase.getInstance(getApplicationContext()).announcementDao();

            boolean hasNewEntry = false;
            String latestTitle = "";

            for (AnnouncementArticle article : articles) {
                AnnouncementEntity existing = dao.getAnnouncementByUrl(article.url);
                if (existing == null) {
                    dao.insert(new AnnouncementEntity(
                            article.url,
                            article.title,
                            article.content,
                            article.submitted.toString()
                    ));
                    hasNewEntry = true;
                    latestTitle = article.title;
                }
            }

            if (hasNewEntry) {
                sendNotification(latestTitle);
            }

            return Result.success();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }

    private void sendNotification(String title) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "New Announcements", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Thông báo mới từ UITHub")
                .setContentText(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}
