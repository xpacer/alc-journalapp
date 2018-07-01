package com.xpacer.journalapp.ui;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.xpacer.journalapp.data.AppDatabase;
import com.xpacer.journalapp.data.GistEntry;

import java.util.List;

/**
 * ViewModel for MainActivity
 */
public class MainViewModel extends AndroidViewModel {

    private LiveData<List<GistEntry>> gists;

    public MainViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        gists = database.gistDao().loadAllGists();
    }

    public LiveData<List<GistEntry>> getGists() {
        return gists;
    }
}
