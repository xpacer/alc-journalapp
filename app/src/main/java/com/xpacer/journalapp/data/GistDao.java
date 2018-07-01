package com.xpacer.journalapp.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface GistDao {

    @Query("SELECT * FROM gists ORDER BY created_at DESC")
    LiveData<List<GistEntry>> loadAllGists();

    @Insert
    void insertGist(GistEntry gistEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateGist(GistEntry gistEntry);

    @Delete
    void deleteGist(GistEntry gistEntry);

    @Query("SELECT * FROM gists WHERE id = :id")
    LiveData<GistEntry> loadGistById(String id);

    @Query("DELETE FROM gists")
    void deleteTable();

}
