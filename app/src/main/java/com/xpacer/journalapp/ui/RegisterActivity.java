package com.xpacer.journalapp.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.xpacer.journalapp.R;
import com.xpacer.journalapp.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private ActivityRegisterBinding mRegisterBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        mRegisterBinding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        mRegisterBinding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mRegisterBinding.editTextEmail.getText().toString();
                String password = mRegisterBinding.editTextPassword.getText().toString();
                register(email, password);
            }
        });

        mRegisterBinding.textViewLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                proceedToLogin();
            }
        });
    }

    private void register(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                proceedToHome(user);
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Unable to register user",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void proceedToHome(FirebaseUser user) {
        Context context = RegisterActivity.this;
        Intent startMainIntent = new Intent(context, MainActivity.class);
        startMainIntent.putExtra(MainActivity.USER_EMAIL_EXTRA, user.getEmail());
        startMainIntent.putExtra(MainActivity.USER_DISPLAY_NAME_EXTRA, user.getDisplayName());

        startActivity(startMainIntent);
    }

    private void proceedToLogin() {
        Context context = RegisterActivity.this;
        Intent startRegistrationIntent = new Intent(context, LoginActivity.class);
        startActivity(startRegistrationIntent);
    }
}
