package com.xpacer.journalapp.ui;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.xpacer.journalapp.utils.AppExecutors;
import com.xpacer.journalapp.R;
import com.xpacer.journalapp.data.AppDatabase;
import com.xpacer.journalapp.data.GistEntry;

import java.util.Date;

public class AddGistDialogFragment extends DialogFragment {

    public static final String GIST_ID_EXTRA_KEY = "gist_id_extra_key";
    private EditText mEditTextCaption;
    private EditText mEditTextContent;
    private Button mButtonSubmit;
    private AppDatabase mDb;
    //When not in update mode
    public static final String DIALOG_TAG = "add_gist_dialog";
    private String mGistId = null;
    private AddGistViewModel viewModel;
    private CollectionReference mGistCollection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);

        if (getActivity() != null)
            mDb = AppDatabase.getInstance(getActivity().getApplicationContext());

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore mFirebaseDb = FirebaseFirestore.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();

        if (mFirebaseUser != null && mFirebaseUser.getEmail() != null)
            mGistCollection = mFirebaseDb.collection(mFirebaseUser.getEmail());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_gist, container, false);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        Dialog addGistDialog = getDialog();

        if (addGistDialog != null && addGistDialog.getWindow() != null)
            addGistDialog.getWindow().setLayout((6 * width) / 7, (4 * height) / 5);

        mEditTextCaption = view.findViewById(R.id.edit_text_caption);
        mEditTextContent = view.findViewById(R.id.edit_text_content);
        mButtonSubmit = view.findViewById(R.id.button_submit);

        setupView();
        return view;
    }

    private void setupView() {

        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubmitButtonClicked();
            }
        });

        if (getArguments() != null && getArguments().containsKey(GIST_ID_EXTRA_KEY)) {
            mButtonSubmit.setText(R.string.update_button_text);
            if (mGistId == null) {
                mGistId = getArguments().getString(GIST_ID_EXTRA_KEY);

                AddGistViewModelFactory factory = new AddGistViewModelFactory(mDb, mGistId);
                viewModel
                        = ViewModelProviders.of(this, factory).get(AddGistViewModel.class);

                viewModel.getGist().observe(this, new Observer<GistEntry>() {
                    @Override
                    public void onChanged(@Nullable GistEntry gistEntry) {
                        viewModel.getGist().removeObserver(this);
                        populateUI(gistEntry);
                    }
                });
            }
        }


    }

    public void onSubmitButtonClicked() {
        Toast toast;

        if (mEditTextCaption.getText().length() == 0) {
            toast = Toast.makeText(getContext(), "Caption must not be empty", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        if (mEditTextContent.getText().length() == 15) {
            toast = Toast.makeText(getContext(), "Content must not be less than 15 characters", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        String caption = mEditTextCaption.getText().toString();
        String content = mEditTextContent.getText().toString();
        Date date = new Date();

        GistEntry gistEntry = new GistEntry(caption, content, date);
        saveToFirestore(gistEntry);
    }

    private void populateUI(GistEntry gist) {
        if (gist == null) {
            return;
        }

        mEditTextCaption.setText(gist.getCaption());
        mEditTextContent.setText(gist.getContent());
    }

    private void saveToLocalDatabase(final GistEntry gistEntry) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mGistId == null) {
                    // insert new gist
                    mDb.gistDao().insertGist(gistEntry);
                } else {
                    // Update gist
                    mDb.gistDao().updateGist(gistEntry);
                }
                dismiss();
            }
        });
    }

    private void saveToFirestore(final GistEntry gistEntry) {
        if (gistEntry == null || mGistCollection == null) {
            return;
        }

        DocumentReference gistDocument;

        if (mGistId == null) {
            gistDocument = mGistCollection.document();
            gistEntry.setId(gistDocument.getId());
        } else {
            GistEntry gistValue = viewModel.getGist().getValue();

            if (gistValue != null) {
                gistEntry.setId(mGistId);
                gistEntry.setCreatedAt(gistValue.getCreatedAt());
            }

            gistDocument = mGistCollection.document(mGistId);

        }

        gistDocument.set(gistEntry)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        saveToLocalDatabase(gistEntry);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "An error occured while trying to save " +
                                "your data. Please try again", Toast.LENGTH_LONG).show();
                    }
                });
    }


}
