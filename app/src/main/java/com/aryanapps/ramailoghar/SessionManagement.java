package com.aryanapps.ramailoghar;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SessionManagement {

    final String PREF_NAME = "LOGIN";
    final String LOG_IN = "IS_LIGIN";
    final String NAME = "NAME";
    final String RANK = "RANK";
    final String EMAIL = "EMAIL";
    final String AMOUNT = "AMOUNT";
    final String ID = "ID";
    final String PROFILE_PIC = "PROFILE_PIC";
    final String SHARED_KEY = "session_user";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;

    public SessionManagement(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveSession(String email, String name, String amount, int id) {
        editor.putBoolean(LOG_IN, true);
        editor.putString(EMAIL, email);
        editor.putString(AMOUNT, amount);
        editor.putString(NAME, name);
        editor.putInt(ID, id);
        editor.apply();

    }

    public boolean isLogin() {
        return sharedPreferences.getBoolean(LOG_IN, false);
    }

    public HashMap<String, String> getUser() {
        HashMap<String, String> user = new HashMap<>();
        user.put(NAME, sharedPreferences.getString(NAME, null));
        user.put(EMAIL, sharedPreferences.getString(EMAIL, null));
        user.put(AMOUNT, sharedPreferences.getString(AMOUNT, "0.00"));
        user.put(ID, String.valueOf(sharedPreferences.getInt(ID, 0)));
        return user;
    }

    public int getId() {
        return sharedPreferences.getInt(ID, 0);
    }

    public String getName() {
        return sharedPreferences.getString(NAME, "Anonymous");
    }

    public void setName(String name) {
        editor.putString(NAME, name);
        editor.apply();
    }

    public String getEmail() {
        return sharedPreferences.getString(EMAIL, "Anonymous@gmail.com");
    }

    public void setEmail(String email) {
        editor.putString(EMAIL, email);
        editor.apply();
    }

    public void setProfilePic() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(String.valueOf(getId()))
                .child("profile_pic");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                editor.putString(PROFILE_PIC, String.valueOf(dataSnapshot.getValue()));
                editor.apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public String getProfilePic() {
        setProfilePic();
        return sharedPreferences.getString(PROFILE_PIC, "");
    }

    public String getAMOUNT() {
        return sharedPreferences.getString(AMOUNT, null);
    }

    public void setAMOUNT(String amount) {
        editor.putString(AMOUNT, amount);
        editor.apply();
    }

    public void logOut() {
        editor.clear();
        editor.commit();
    }

    public String getRank() {
        return sharedPreferences.getString(RANK, "unknown");
    }

    public void setRank(String rank) {
        editor.putString(RANK, rank);
        editor.apply();

    }


}
