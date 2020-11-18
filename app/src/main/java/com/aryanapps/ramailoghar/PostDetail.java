package com.aryanapps.ramailoghar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aryanapps.ramailoghar.Adapter.PostAdapter;
import com.aryanapps.ramailoghar.model.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostDetail extends AppCompatActivity {

    RecyclerView recyclerView;
    PostAdapter postAdapter;
    List<Post> posts;
    String postId;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        postId = sharedPreferences.getString("postId", "none");
        recyclerView = findViewById(R.id.recycler_view);
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getApplicationContext(), posts);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(postAdapter);
        readFromDataabse();
    }

    private void readFromDataabse() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Post")
                .child(postId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                posts.clear();
                Post post = dataSnapshot.getValue(Post.class);
                posts.add(post);
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}