package com.example.mad_project_draft_2;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseHelper {

    public static void fetchChatbotCredentials(final ChatbotCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("chatbot_credentials");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String apiKey = dataSnapshot.child("api_key").getValue(String.class);
                    callback.onCredentialsFetched(apiKey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public interface ChatbotCallback {
        void onCredentialsFetched(String apiKey);
        void onError(String error);
    }
}
