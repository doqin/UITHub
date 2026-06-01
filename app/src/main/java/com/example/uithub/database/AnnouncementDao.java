package com.example.uithub.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AnnouncementDao {
    @Query("SELECT * FROM ANNOUNCEMENT WHERE url = :url LIMIT 1")
    AnnouncementEntity getAnnouncementByUrl(String url);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AnnouncementEntity announcement);

    @Query("SELECT COUNT(*) FROM ANNOUNCEMENT")
    int getCount();
}
