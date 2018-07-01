package com.xpacer.journalapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.xpacer.journalapp.data.AppDatabase;
import com.xpacer.journalapp.data.GistEntry;
import com.xpacer.journalapp.databinding.ActivityMainBinding;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements JournalGistAdapter.ItemClickListener {

    public static final String USER_DISPLAY_NAME_EXTRA = "user_display_name";
    public static final String USER_EMAIL_EXTRA = "user_email";

    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private ActivityMainBinding mBinding;
    private JournalGistAdapter mGistAdapter;
    private AppDatabase mDb;
    private MainViewModel mViewModel;
    private CollectionReference mGistCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setUpView();

        mDb = AppDatabase.getInstance(getApplicationContext());
        FirebaseFirestore mFirebaseDb = FirebaseFirestore.getInstance();

        if (mFirebaseUser != null && mFirebaseUser.getEmail() != null)
            mGistCollection = mFirebaseDb.collection(mFirebaseUser.getEmail());

        setupViewModel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.action, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.menu_settings:
                //TODO: Go to settings Page
                break;
            case R.id.menu_logout:
                logout();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.gistDao().deleteTable();
            }
        });
        mAuth.signOut();
        returnToLogin();
    }

    private void returnToLogin() {
        Context context = MainActivity.this;
        Intent startLoginIntent = new Intent(context, LoginActivity.class);
        startActivity(startLoginIntent);
    }

    private void setUpView() {
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        mBinding.recyclerViewGists.setLayoutManager(gridLayoutManager);
        mBinding.recyclerViewGists.setHasFixedSize(true);

        mBinding.textViewEnterGist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addGist();
            }
        });

        mGistAdapter = new JournalGistAdapter(this, this);
        mBinding.recyclerViewGists.setAdapter(mGistAdapter);

    }

    private void setupViewModel() {
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mViewModel.getGists().observe(this, new Observer<List<GistEntry>>() {
            @Override
            public void onChanged(@Nullable List<GistEntry> gistEntries) {
                mGistAdapter.setJournalData(gistEntries);
            }
        });

        List<GistEntry> gistEntries = mViewModel.getGists().getValue();

        if (gistEntries == null || gistEntries.size() == 0)
            initFromFirestoreCollection();

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

    private void addGist() {
        FragmentTransaction fragmentTransaction = getFragmentTransaction();
        DialogFragment dialogFragment = new AddGistDialogFragment();
        dialogFragment.show(fragmentTransaction, AddGistDialogFragment.DIALOG_TAG);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onItemClickListener(String itemId) {
        Intent startDetailActivity = new Intent(MainActivity.this, GistDetailActivity.class);
        startDetailActivity.putExtra(GistDetailActivity.GIST_ID_EXTRA_KEY, itemId);
        startActivity(startDetailActivity);
    }

    public void initFromFirestoreCollection() {
        mGistCollection
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                saveToLocalDatabase(document.getData());
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private void saveToLocalDatabase(Map<String, Object> gistData) {
        final GistEntry gistEntry = new GistEntry(gistData);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.gistDao().insertGist(gistEntry);
            }
        });
    }
}
