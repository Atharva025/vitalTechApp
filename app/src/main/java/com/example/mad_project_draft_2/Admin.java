package com.example.mad_project_draft_2;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class Admin extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> appointmentList;
    private List<Appointment> filteredAppointmentList;
    private DatabaseReference databaseReference;
    private EditText searchEditText;
    private Button logoutButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        recyclerView = findViewById(R.id.recyclerViewAppointments);
        logoutButton = findViewById(R.id.buttonLogout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchEditText = findViewById(R.id.editTextSearch);

        appointmentList = new ArrayList<>();
        filteredAppointmentList = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(this, filteredAppointmentList);
        recyclerView.setAdapter(appointmentAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Appointments");
        fetchAppointments();

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext() , Login.class);
                startActivity(intent);
                finish();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterAppointments(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void fetchAppointments() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                        Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                        appointmentList.add(appointment);
                    }

                    filteredAppointmentList.addAll(appointmentList);
                    appointmentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Admin.this, "Failed to load appointments.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAppointments(String query) {
        filteredAppointmentList.clear();
        if (query.isEmpty()) {
            filteredAppointmentList.addAll(appointmentList);
        } else {
            for (Appointment appointment : appointmentList) {
                if (appointment.getUserName().toLowerCase().contains(query.toLowerCase())) {
                    filteredAppointmentList.add(appointment);
                }
            }
        }
        appointmentAdapter.notifyDataSetChanged();
    }
}
