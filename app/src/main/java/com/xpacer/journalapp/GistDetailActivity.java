package com.xpacer.journalapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.xpacer.journalapp.data.AppDatabase;
import com.xpacer.journalapp.data.GistEntry;
import com.xpacer.journalapp.databinding.ActivityGistDetailBinding;

public class GistDetailActivity extends AppCompatActivity {

    public static final String GIST_ID_EXTRA_KEY = "gist_id_extra_key";
    private static final String TAG = GistDetailActivity.class.getSimpleName();
    private String mGistId;
    private AppDatabase mDb;
    private ActivityGistDetailBinding mBinding;
    private GistDetailViewModel viewModel;
    private CollectionReference mGistCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gist_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore mFirebaseDb = FirebaseFirestore.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();

        if (mFirebaseUser != null && mFirebaseUser.getEmail() != null)
            mGistCollection = mFirebaseDb.collection(mFirebaseUser.getEmail());


        Intent intent = getIntent();
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_gist_detail);
        mDb = AppDatabase.getInstance(this);
        if (intent != null && intent.hasExtra(GIST_ID_EXTRA_KEY)) {
            mGistId = intent.getStringExtra(GIST_ID_EXTRA_KEY);

            GistDetailViewModelFactory factory = new GistDetailViewModelFactory(mDb, mGistId);

            viewModel = ViewModelProviders.of(this, factory).get(GistDetailViewModel.class);
            viewModel.getGist().observe(this, new Observer<GistEntry>() {
                @Override
                public void onChanged(@Nullable GistEntry gistEntry) {
                    gistEntry = viewModel.getGist().getValue();
                    populateUI(gistEntry);
                }

            });
        }

    }


    void populateUI(GistEntry gistEntry) {
        if (gistEntry == null)
            return;

        mBinding.textViewDetailContent.setText(gistEntry.getContent());
        mBinding.textViewDetailCaption.setText(gistEntry.getCaption());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.gist_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        }

        if (itemId == R.id.edit_gist) {
            editGist(mGistId);
            return true;
        }

        if (itemId == R.id.delete_gist) {
            deleteGist();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private FragmentTransaction getFragmentTransaction() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag(AddGistDialogFragment.DIALOG_TAG);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }

        fragmentTransaction.addToBackStack(null);

        return fragmentTransaction;
    }

    private void editGist(String gistId) {
        FragmentTransaction fragmentTransaction = getFragmentTransaction();
        Bundle bundle = new Bundle();
        bundle.putString(AddGistDialogFragment.GIST_ID_EXTRA_KEY, gistId);
        DialogFragment dialogFragment = new AddGistDialogFragment();
        dialogFragment.show(fragmentTransaction, AddGistDialogFragment.DIALOG_TAG);
        dialogFragment.setArguments(bundle);
    }

    private void deleteGist() {
        mGistCollection.document(mGistId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                GistEntry gist = viewModel.getGist().getValue();
                                mDb.gistDao().deleteGist(gist);
                            }
                        });
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });

    }
}


