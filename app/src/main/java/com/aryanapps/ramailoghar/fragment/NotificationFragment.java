package com.aryanapps.ramailoghar.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.aryanapps.ramailoghar.Adapter.NotificatonAdapter;
import com.aryanapps.ramailoghar.R;
import com.aryanapps.ramailoghar.SessionManagement;
import com.aryanapps.ramailoghar.model.Notification;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class NotificationFragment extends Fragment {
    RecyclerView recyclerView;
    NotificatonAdapter notificatonAdapter;
    List<Notification> notifications;
    LottieAnimationView animation_view;
    TextView no_notifications;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        animation_view =  view.findViewById(R.id.animation_view);
        no_notifications =  view.findViewById(R.id.nopost);
        recyclerView = view.findViewById(R.id.recyclerView);
        notifications = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        notificatonAdapter = new NotificatonAdapter(getContext(),notifications);
        recyclerView.setAdapter(notificatonAdapter);
        readFromDatabase();
        return view;
    }

    private void readFromDatabase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Notification")
                .child(String.valueOf(new SessionManagement(getContext()).getId()));
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notifications.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    notifications.add(notification);
                }
                Collections.reverse(notifications);
                notificatonAdapter.notifyDataSetChanged();
                if(notifications.size() == 0){
                    animation_view.setVisibility(View.VISIBLE);
                    no_notifications.setVisibility(View.VISIBLE);
                }else {
                    animation_view.setVisibility(View.GONE);
                    no_notifications.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}