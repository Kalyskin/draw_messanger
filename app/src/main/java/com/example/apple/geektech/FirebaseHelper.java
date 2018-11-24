package com.example.apple.geektech;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {

    private static FirebaseHelper instance;
    private Context context;

    public DatabaseReference getDatabase() {
        return mDatabase;
    }

    private DatabaseReference mDatabase;

    public FirebaseHelper(Context context){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        this.context = context;
    }

    public static void init(Context context) {
         instance = new FirebaseHelper(context);
    }

    public static FirebaseHelper getInstance() {
        return instance;
    }

    public String addUser(){
        return mDatabase.child("users").push().getKey();
    }
}