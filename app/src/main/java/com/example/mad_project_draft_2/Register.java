package com.example.mad_project_draft_2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

public class Register extends AppCompatActivity {

    private TextView textViewTitleRegister, textViewLogin;
    private EditText editTextEmailRegister, editTextPasswordRegister;
    private Button buttonSignUp;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        textViewTitleRegister = findViewById(R.id.textViewTitleRegister);
        editTextEmailRegister = findViewById(R.id.editTextEmailRegister);
        editTextPasswordRegister = findViewById(R.id.editTextPasswordRegister);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textViewLogin = findViewById(R.id.textViewLogin);
        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();

        textViewLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        buttonSignUp.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = String.valueOf(editTextEmailRegister.getText());
            String password = String.valueOf(editTextPasswordRegister.getText());

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Register.this, "Enter Email", Toast.LENGTH_LONG).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Register.this, "Enter Password", Toast.LENGTH_LONG).show();
                return;
            }

            checkEmailExists(email);
        });
    }

    private void checkEmailExists(String email) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            if (result.getSignInMethods().isEmpty()) {
                                registerUser(email);
                            } else {
                                Toast.makeText(Register.this, "Email already exists. Please login.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(Register.this, "Error checking email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void registerUser(String email) {
        String password = String.valueOf(editTextPasswordRegister.getText());
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(Register.this, "Authentication Successful", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(Register.this, "Authentication Failed.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
