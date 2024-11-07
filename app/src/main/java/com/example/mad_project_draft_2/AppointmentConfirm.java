package com.example.mad_project_draft_2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AppointmentConfirm extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private TextView textViewNameValue, textViewEmailValue, textViewPhoneNumberValue, textViewAppointmentDetailsValue;
    private Button logOutButton , buttonSpeakWithChatbot;
    private FirebaseAuth mAuth; // Declare FirebaseAuth variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_confirm);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        textViewNameValue = findViewById(R.id.textViewNameValue);
        buttonSpeakWithChatbot = findViewById(R.id.buttonSpeakWithChatbot);
        textViewEmailValue = findViewById(R.id.textViewEmailValue);
        textViewPhoneNumberValue = findViewById(R.id.textViewPhoneNumberValue);
        textViewAppointmentDetailsValue = findViewById(R.id.textViewAppointmentDetailsValue);
        logOutButton = findViewById(R.id.buttonLogout);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            logOutButton.setText(currentUser.getEmail());
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Preventing app from closing when back button is pressed
                // Explicitly return to the previous activity
                Intent intent = new Intent(AppointmentConfirm.this, Book_Appointment.class);
                startActivity(intent);
                finish();
            }
        };
        // Adding the callback to the activity's OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);

        buttonSpeakWithChatbot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appointmentIdForChatbot = getIntent().getStringExtra("appointmentId");
                Intent intent = new Intent(getApplicationContext() , Chatbot.class);
                intent.putExtra("appointmentIdForChatbot", appointmentIdForChatbot);
                startActivity(intent);
                finish();
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Appointments");

        // Fetch appointment details
        fetchAppointmentDetails();
    }

    private void fetchAppointmentDetails() {
        // Get the appointment ID passed from the previous activity
        String appointmentId = getIntent().getStringExtra("appointmentId");
        Log.d("AppointmentConfirm", "Received Appointment ID: " + appointmentId);

        if (appointmentId != null) {
            // Fetch appointment data from the database using the appointment ID
            databaseReference.child(appointmentId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Get the data from the snapshot
                        String name = snapshot.child("userName").getValue(String.class);
                        String email = snapshot.child("userEmail").getValue(String.class);
                        String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                        String date = snapshot.child("date").getValue(String.class);
                        String time = snapshot.child("time").getValue(String.class);

                        // Set the values to the UI elements
                        textViewNameValue.setText(name != null ? name : "N/A");
                        textViewEmailValue.setText(email != null ? email : "N/A");
                        textViewPhoneNumberValue.setText(phoneNumber != null ? phoneNumber : "N/A");
                        textViewAppointmentDetailsValue.setText(date != null ? "Date: " + date + "\nTime: " + time : "No appointment details available");
                    } else {
                        Toast.makeText(AppointmentConfirm.this, "Appointment not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AppointmentConfirm.this, "Failed to load appointment details", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(AppointmentConfirm.this, "Appointment ID is missing", Toast.LENGTH_SHORT).show();
        }
    }
}
