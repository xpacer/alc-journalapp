package com.xpacer.journalapp.ui.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.xpacer.journalapp.data.AppDatabase;
import com.xpacer.journalapp.data.GistEntry;

public class AddGistViewModel extends ViewModel {
    private LiveData<GistEntry> gistEntry;

    public AddGistViewModel(AppDatabase database, String gistId) {
        gistEntry = database.gistDao().loadGistById(gistId);
    }

    public LiveData<GistEntry> getGist() {
        return gistEntry;
    }
}

