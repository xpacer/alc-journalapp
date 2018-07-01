package com.xpacer.journalapp.ui;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.xpacer.journalapp.data.AppDatabase;

public class GistDetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final String mGistId;

    public GistDetailViewModelFactory(AppDatabase database, String gistId) {
        mDb = database;
        mGistId = gistId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new GistDetailViewModel(mDb, mGistId);
    }
}
