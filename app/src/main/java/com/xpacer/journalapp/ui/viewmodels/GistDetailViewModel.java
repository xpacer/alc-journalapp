package com.xpacer.journalapp.ui.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.xpacer.journalapp.data.AppDatabase;
import com.xpacer.journalapp.data.GistEntry;

public class GistDetailViewModel extends ViewModel {
    private LiveData<GistEntry> gistEntry;

    public GistDetailViewModel(AppDatabase database, String gistId) {
        gistEntry = database.gistDao().loadGistById(gistId);
    }

    public LiveData<GistEntry> getGist() {
        return gistEntry;
    }
}
