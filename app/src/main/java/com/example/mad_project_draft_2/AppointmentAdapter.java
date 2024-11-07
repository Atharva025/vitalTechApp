package com.example.mad_project_draft_2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private Context context;
    private List<Appointment> appointmentList;

    public AppointmentAdapter(Context context, List<Appointment> appointmentList) {
        this.context = context;
        this.appointmentList = appointmentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);

        // Set data on the TextViews
        holder.textViewName.setText("Name: " + appointment.getUserName());
        holder.textViewEmail.setText("Email: " + appointment.getUserEmail());
        holder.textViewPhone.setText("Phone: " + appointment.getPhoneNumber());
        holder.textViewDateAndTime.setText("Date and Time: " + appointment.getDate() + " " + appointment.getTime());


        // Handle Confirm button click
        holder.buttonConfirm.setOnClickListener(v -> {
            String message = "Greetings from VitalTech.\nYour appointment has been confirmed!\nName: " + appointment.getUserName() +
                    "\nEmail: " + appointment.getUserEmail() + "\nOn Date: " + appointment.getDate() +
                    "\nAt Time: " + appointment.getTime();
            sendSmsIntent(appointment.getPhoneNumber(), message);
        });

        // Handle Cancel button click
        holder.buttonCancel.setOnClickListener(v -> {
            String message = "Greetings, we regret to inform you that your appointment has been cancelled. " +
                    "Please consider booking for another day as our doctors are busy today.";
            sendSmsIntent(appointment.getPhoneNumber(), message);
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewDateAndTime , textViewEmail , textViewPhone;
        Button buttonConfirm, buttonCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Ensure IDs match with item_appointment.xml
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewPhone = itemView.findViewById(R.id.textViewPhone);
            textViewDateAndTime = itemView.findViewById(R.id.textViewDateTime);
            buttonConfirm = itemView.findViewById(R.id.buttonConfirm);
            buttonCancel = itemView.findViewById(R.id.buttonCancel);
        }
    }

    private void sendSmsIntent(String phoneNumber, String message) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setData(Uri.parse("smsto:" + phoneNumber));  // Only SMS apps handle this
        smsIntent.putExtra("sms_body", message);

        try {
            context.startActivity(smsIntent);
        } catch (Exception e) {
            Toast.makeText(context, "SMS app not found.", Toast.LENGTH_SHORT).show();
        }
    }
}
