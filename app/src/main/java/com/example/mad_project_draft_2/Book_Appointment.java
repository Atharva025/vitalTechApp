package com.example.mad_project_draft_2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mad_project_draft_2.databinding.ActivityBookAppointmentBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class Book_Appointment extends AppCompatActivity {

    ActivityBookAppointmentBinding binding;
    String name, email, phoneNumber, bloodGroup, pastProblems, familyMedicalHistory, problemDescription, date, time;
    FirebaseDatabase database;
    DatabaseReference reference;
    private FirebaseAuth mAuth;
    private Button logOutButton, setTime, buttonSetDate;
    private TextView textViewSelectedTime , textViewSelectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookAppointmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        logOutButton = findViewById(R.id.buttonLogout);
        setTime = findViewById(R.id.buttonSetTime);
        textViewSelectedTime = findViewById(R.id.textViewSelectedTime);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        buttonSetDate = findViewById(R.id.buttonSetDate);
        reference = FirebaseDatabase.getInstance().getReference("Appointments");
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            binding.editTextEmail.setText(currentUser.getEmail());
            logOutButton.setText(currentUser.getEmail());
        }

        binding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        buttonSetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(Book_Appointment.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                        textViewSelectedDate.setText(date);
                    }
                } , year , month , day);
                datePickerDialog.show();
            }
        });

        setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(Book_Appointment.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String time = String.format("%02d:%02d", hourOfDay, minute);
                        textViewSelectedTime.setText(time);
                    }
                }, hour, minute, true);

                timePickerDialog.show();
            }
        });

        binding.buttonBookAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookAppointment();
            }
        });
    }

    public void bookAppointment() {
        name = binding.editTextName.getText().toString().trim();
        email = binding.editTextEmail.getText().toString().trim();
        phoneNumber = binding.editTextPhoneNumber.getText().toString().trim();
        bloodGroup = binding.editTextBloodGroup.getText().toString().trim();
        pastProblems = binding.editTextPastProblems.getText().toString().trim();
        familyMedicalHistory = binding.editTextFamilyHistory.getText().toString().trim();
        problemDescription = binding.editTextProblemDescription.getText().toString().trim();
        date = binding.textViewSelectedDate.getText().toString().trim();
        time = binding.textViewSelectedTime.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(bloodGroup) ||
                TextUtils.isEmpty(pastProblems) || TextUtils.isEmpty(familyMedicalHistory) || TextUtils.isEmpty(problemDescription)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show();
            return;
        }

        Appointment appointment = new Appointment(name, email, phoneNumber, bloodGroup, pastProblems, familyMedicalHistory, problemDescription, date, time);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Appointments");

        DatabaseReference newAppointmentRef = reference.push();  // Generates a unique key
        String appointmentId = newAppointmentRef.getKey();
        Log.d("Book_Appointment", "Generated Appointment ID: " + appointmentId); // Log the ID for debugging

        newAppointmentRef.setValue(appointment).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                binding.editTextName.setText("");
                binding.editTextEmail.setText("");
                binding.editTextPhoneNumber.setText("");
                binding.editTextBloodGroup.setText("");
                binding.editTextPastProblems.setText("");
                binding.editTextFamilyHistory.setText("");
                binding.editTextProblemDescription.setText("");
                binding.textViewSelectedDate.setText("");
                binding.textViewSelectedTime.setText("");
                Toast.makeText(Book_Appointment.this, "Appointment booked successfully", Toast.LENGTH_LONG).show();
            }
        });
        Intent intent = new Intent(getApplicationContext(), AppointmentConfirm.class);
        intent.putExtra("appointmentId", appointmentId);
        startActivity(intent);
        finish();
    }
}
