package com.example.mad_project_draft_2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Chatbot extends AppCompatActivity {

    private EditText userQueryEditText;
    private DatabaseReference databaseReference;
    private LinearLayout conversationLayout;
    private String geminiApiKey;
    private FirebaseAuth mAuth;
    private String userName = "User";
    private String familyMedicalHistory , pastProblems , problemDescription;
    private Button sendButton , buttonLogout;
    private TextView textViewBotMessage, textViewUserMessage;
    private List<String> conversationHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        userQueryEditText = findViewById(R.id.editTextUserMessage);
        conversationLayout = findViewById(R.id.conversationLayout);
        textViewBotMessage = findViewById(R.id.textViewBotMessage);
        textViewUserMessage = findViewById(R.id.textViewUserMessage);
        buttonLogout = findViewById(R.id.buttonLogout);
        sendButton = findViewById(R.id.buttonSend);
        mAuth = FirebaseAuth.getInstance();

        String appointmentIdForChatbot = getIntent().getStringExtra("appointmentIdForChatbot");

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Preventing app from closing when back button is pressed
                // Explicitly return to the previous activity
                Intent intent = new Intent(Chatbot.this, AppointmentConfirm.class);
                intent.putExtra("appointmentId", appointmentIdForChatbot);
                startActivity(intent);
                finish();
            }
        };
        // Adding the callback to the activity's OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);


        databaseReference = FirebaseDatabase.getInstance().getReference("Appointments");
        String appointmentId = getIntent().getStringExtra("appointmentIdForChatbot");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d("Chatbot", "Current user ID: " + currentUser.getUid());
        } else {
            Log.d("Chatbot", "No user is currently logged in.");
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext() , Login.class);
                startActivity(intent);
                finish();
            }
        });

        if (appointmentId != null) {
            databaseReference.child(appointmentId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        userName = snapshot.child("userName").getValue(String.class);
                        familyMedicalHistory = snapshot.child("familyMedicalHistory").getValue(String.class);
                        pastProblems = snapshot.child("pastProblems").getValue(String.class);
                        problemDescription = snapshot.child("problemDescription").getValue(String.class);
                        Log.d("Chatbot", "userName retrieved: " + userName);
                    } else {
                        Log.d("Chatbot", "No userName found for the appointmentId");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("Chatbot", "Database error: " + error.getMessage());
                }
            });
        } else {
            Log.d("Chatbot", "appointmentId is null");
        }

        fetchChatbotCredentials();

        sendButton.setOnClickListener(v -> sendUserQuery());
    }



    private void fetchChatbotCredentials() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("chatbot_credentials/api_key");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    geminiApiKey = dataSnapshot.getValue(String.class);
                    sendMessageToGemini("You are an AI healthcare assistant for an online diagnostic platform. Using the detailed patient report from the database, respond to the patient's current medical situation based on their existing conditions, medical history, and other relevant data. Provide a general indication of possible conditions related to their symptoms, along with the level of urgency (e.g., emergency, urgent, non-urgent). Offer general advice such as lifestyle adjustments or precautions to be taken in the meantime, and emphasize the importance of seeking professional consultation for a thorough evaluation and treatment plan. Ensure your response is addressed directly to the user in the second person (e.g., 'you should'), and avoid giving specific medical instructions or treatment recommendations. Generate a professional medical report that includes the following sections: Patient Report Summary of Findings Possible Condition(s): [list only conditions] Urgency Level: Urgent Recommended Specialist: Neurologist/Neurosurgeon Next Steps Consultation: Schedule an appointment with a neurologist or neurosurgeon to discuss diagnosis and treatment options. Lifestyle Adjustments: Maintain a healthy lifestyle, including a balanced diet and regular exercise. Symptom Monitoring: Track changes in symptoms and report to the healthcare provider promptly. Additional Information Follow healthcare provider recommendations closely and stay informed about the condition. Make sure the tone is professional and factual, without including any empathetic phrases or emotional language. and give all answers in formal report structure in detail. Only answer medical and healthcare questions. Here's the patient details: \n Name: " + userName + "\nFamily Medical History: " + familyMedicalHistory + "\nPast Medical Conditions of the patient: " + pastProblems + "\nActual Problem of the patient: " + problemDescription);
                    sendMessageToBot("Greetings " + userName + "\n" + "I can help you with medical questions.");
                } else {
                    Toast.makeText(Chatbot.this, "Error: No API key found in Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Chatbot.this, "Error fetching credentials: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendUserQuery() {
        String userQuery = userQueryEditText.getText().toString();
        if (!userQuery.isEmpty()) {
            addMessageToConversation(userName + ": " + userQuery, true);
            sendMessageToGemini("You are an AI healthcare assistant for an online diagnostic platform. Using the data provided to you respond to the patient's current medical situation based on their existing conditions, medical history, and other relevant data. Provide a general indication of possible conditions related to their symptoms, along with the level of urgency (e.g., emergency, urgent, non-urgent). Offer general advice such as lifestyle adjustments or precautions to be taken in the meantime, and emphasize the importance of seeking professional consultation for a thorough evaluation and treatment plan. Ensure your response is addressed directly to the user in the second person (e.g., 'you should'), and avoid giving specific medical instructions or treatment recommendations. Generate a professional medical report that includes the following sections: Patient Report Summary of Findings Possible Condition(s): [list only conditions] Urgency Level: Urgent Recommended Specialist: Neurologist/Neurosurgeon Next Steps Consultation: Schedule an appointment with a neurologist or neurosurgeon to discuss diagnosis and treatment options. Lifestyle Adjustments: Maintain a healthy lifestyle, including a balanced diet and regular exercise. Symptom Monitoring: Track changes in symptoms and report to the healthcare provider promptly. Additional Information Follow healthcare provider recommendations closely and stay informed about the condition. Make sure the tone is professional and factual, without including any empathetic phrases or emotional language. and give all answers in formal report structure in detail. If the user deviates from medical or healthcare questions, just shut him/her up in a sarcastic way " + problemDescription + ". User query starts from here " + userQuery);
            userQueryEditText.setText("");
        } else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessageToGemini(String message) {
        if (geminiApiKey == null) {
            Toast.makeText(this, "API key not available.", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        String url = "https://generativelanguage.googleapis.com/v1beta/tunedModels/medicalchatbotsemitraining-57dqz87wdz1i:generateContent?key=" + geminiApiKey;
        String jsonRequestBody = "{ \"contents\": [ { \"parts\": [ { \"text\": \"" + message + "\" } ] } ] }";

        Request request = new Request.Builder()
                .url(url)
                .post(okhttp3.RequestBody.create(jsonRequestBody, okhttp3.MediaType.parse("application/json")))
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("GeminiResponse", responseBody);

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String botReply = jsonResponse.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .optString("text", "Sorry, I couldn't get an answer for you.");

                        runOnUiThread(() -> addMessageToConversation("Bot: " + botReply, false));

                        conversationHistory.add(botReply);
                    } catch (Exception e) {
                        runOnUiThread(() -> addMessageToConversation("Bot: Error parsing response.", false));
                        e.printStackTrace();
                    }

                } else {
                    runOnUiThread(() -> addMessageToConversation("Bot: Sorry, I couldn't get an answer for you.", false));
                }
            } catch (Exception e) {
                runOnUiThread(() -> addMessageToConversation("Bot: Error occurred while fetching response.", false));
                e.printStackTrace();
            }
        }).start();
    }

    private void addMessageToConversation(String message, boolean isUserMessage) {
        TextView textView;

        if (isUserMessage) {
            textView = findViewById(R.id.textViewUserMessage);
            textView.setText(message);
            textView.setGravity(View.TEXT_ALIGNMENT_TEXT_END);
        } else {
            textView = findViewById(R.id.textViewBotMessage);
            textView.setText(message);
            textView.setGravity(View.TEXT_ALIGNMENT_TEXT_START);
        }

        if (textView.getParent() != null) {
            ((ViewGroup) textView.getParent()).removeView(textView);
        }

        conversationLayout.addView(textView);

        ScrollView scrollView = findViewById(R.id.scrollViewConversation);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

    private void sendMessageToBot(String message) {
        addMessageToConversation("Bot: " + message, false);
    }

}
