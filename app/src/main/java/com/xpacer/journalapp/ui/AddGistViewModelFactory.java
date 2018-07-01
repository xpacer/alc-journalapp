package com.xpacer.journalapp.ui;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.xpacer.journalapp.data.AppDatabase;
import com.xpacer.journalapp.ui.viewmodels.AddGistViewModel;

public class AddGistViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppDatabase mDb;
    private final String mGistId;

    public AddGistViewModelFactory(AppDatabase database, String gistId) {
        mDb = database;
        mGistId = gistId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new AddGistViewModel(mDb, mGistId);
    }
}
