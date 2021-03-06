package com.example.apple.geektech.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apple.geektech.MyFirebaseMessagingService;
import com.example.apple.geektech.R;
import com.example.apple.geektech.Utils.SharedPreferenceHelper;
import com.example.apple.geektech.api.NotificationApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.r0adkll.slidr.Slidr;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    Button sendRequestBtn, declineFriendRequestBtn;
    ImageView profilePhoto;
    TextView statusTV, contactNameTV;
    final String TAG = ProfileActivity.class.getSimpleName();
    DatabaseReference userReference;
    DatabaseReference friendRequestReference, notificationsReference;
    private String CURRENT_STATE;
    String receiver_token;
    String receiver_uid;
    String sender_id;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Slidr.attach(this);
        setContentView(R.layout.activity_profile);
        init();
        if (sender_id != null && receiver_token != null) {
            userReference.child(receiver_token).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String photo = dataSnapshot.child("user_photo").getValue().toString();
//                String status = dataSnapshot.child("user_status").getValue().toString();

//                statusTV.setText(status);

                    friendRequestReference.child(sender_id)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(receiver_token)) {
                                        String req_type = dataSnapshot.child(receiver_token).child("request_type").getValue().toString();
                                        if (req_type.equals("sent")) {
                                            CURRENT_STATE = "request_sent";
                                        } else CURRENT_STATE = "";
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            sendRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFriendRequest();
                }
            });
        }
    }

    private void sendFriendRequest() {
        sendNotification();
        friendRequestReference.child(sender_id).child(receiver_token).child("request_type")
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    friendRequestReference.child(receiver_token).child(sender_id)
                            .child("request_type").setValue("receiver")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        CURRENT_STATE = "request_sent";
                                        sendRequestBtn.setText("Cancel friend request");
                                        sendRequestBtn.setEnabled(true);
                                    }

                                }
                            });
                }
            }
        });
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.putExtra("name", getIntent().getStringExtra("name"));
        intent.putExtra("receiver_token", receiver_uid);
        startActivity(intent);
        finish();

    }

    private void sendNotification() {
        String phoneNumber = mAuth.getCurrentUser().getPhoneNumber();
        String body =  " wants to send a message.";

        Map data = new HashMap();
        data.put("title", "Notification");
        data.put("phone", phoneNumber);
        data.put("body", body);
        data.put("id", mAuth.getCurrentUser().getUid());
        data.put("sender_token", SharedPreferenceHelper.getString(ProfileActivity.this, "token", "no token"));

        NotificationApi.send(receiver_token, MyFirebaseMessagingService.TYPE_INVITE_REQUEST, data);

    }

    private void init() {
        sendRequestBtn = findViewById(R.id.send_friend_request_btn);
        declineFriendRequestBtn = findViewById(R.id.decline_friend_request_btn);
        profilePhoto = findViewById(R.id.profileImage);
        contactNameTV = findViewById(R.id.contactNameTV);
        statusTV = findViewById(R.id.status_TV);

        notificationsReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
        notificationsReference.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        sender_id = mAuth.getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("users");
        userReference.keepSynced(true);
        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        friendRequestReference.keepSynced(true);
        friendRequestReference.getParent().keepSynced(true);


        CURRENT_STATE = "not_friend";

        getIntentExtras();


    }

    private void getIntentExtras() {

        if (getIntent().hasExtra("name")) {
            contactNameTV.setText(getIntent().getStringExtra("name"));
            receiver_token = getIntent().getStringExtra("receiver_token");
            receiver_uid = getIntent().getStringExtra("uid");
//            Log.e(TAG, "getIntentExtras: ID " + receiver_token);
//            Log.e(TAG, "getIntentExtras: uID " + receiver_uid );
//            Toast.makeText(this, receiver_token + "", Toast.LENGTH_SHORT).show();
        }
    }
}
