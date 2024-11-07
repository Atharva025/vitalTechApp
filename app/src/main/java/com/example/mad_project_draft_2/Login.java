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

public class Login extends AppCompatActivity {

    private TextView textViewTitleLogin, textViewRegister;
    private EditText editTextEmailLogin, editTextPasswordLogin;
    private Button buttonLogin;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        textViewTitleLogin = findViewById(R.id.textViewWelcome);
        editTextEmailLogin = findViewById(R.id.editTextEmail);
        editTextPasswordLogin = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        progressBar = findViewById(R.id.progressBar);

        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), Book_Appointment.class);
            startActivity(intent);
            finish();
        }

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email = editTextEmailLogin.getText().toString().trim();  // Trimmed
                String password = editTextPasswordLogin.getText().toString().trim();  // Trimmed

                // Debugging logs to check entered values
                Log.d("Login", "Entered Email: " + email);
                Log.d("Login", "Entered Password: " + password);

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Login.this, "Enter Email", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE); // Hide progress bar
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Login.this, "Enter Password", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE); // Hide progress bar
                    return;
                }

                // Check for admin credentials
                if (email.equals("vitalTechAdmin123@gmail.com") && password.equals("vitalTechAdmin123")) {
                    // Admin login
                    Intent intent = new Intent(getApplicationContext(), Admin.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Normal user login via Firebase
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Toast.makeText(Login.this, "Logging In Successful", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(getApplicationContext(), Book_Appointment.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(Login.this, "Logging In Failed", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), Book_Appointment.class);
            startActivity(intent);
            finish();
        }
    }
}
