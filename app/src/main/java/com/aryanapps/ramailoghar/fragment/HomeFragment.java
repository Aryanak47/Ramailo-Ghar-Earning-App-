package com.aryanapps.ramailoghar.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.aryanapps.ramailoghar.Adapter.PostAdapter;
import com.aryanapps.ramailoghar.R;
import com.aryanapps.ramailoghar.SessionManagement;
import com.aryanapps.ramailoghar.model.Post;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    PostAdapter postAdapter;
    List<Post> posts;
    DataSnapshot Snapshot;

    TextView no_post;
    int page=1;
    ProgressBar progressBar;
    ShimmerFrameLayout shimmerContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_home, container, false);
       shimmerContainer = (ShimmerFrameLayout) view.findViewById(R.id.shimer);
        no_post = view.findViewById(R.id.nopost);
        progressBar = view.findViewById(R.id.progressBarLoading);
        progressBar.setVisibility(View.INVISIBLE);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        // TODO COME BACK IN FUTURE
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(),posts);
        recyclerView.setAdapter(postAdapter);

        shimmerContainer.setVisibility(View.VISIBLE);
        shimmerContainer.startShimmerAnimation();

        MobileAds.initialize(getActivity(),"ca-app-pub-8666249229398423~7270644498");

        getLikedPost();
        return view;
    }



    private  RecyclerView.OnScrollListener scrollChangeListener = new RecyclerView.OnScrollListener(){
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if(isLastScroll(recyclerView)) {
                progressBar.setVisibility(View.VISIBLE);
                getLikedPost();
            }
        }
    };

    private boolean isLastScroll(RecyclerView recyclerView) {
        if(recyclerView.getAdapter().getItemCount() != 0) {
            //get the last visible item on screen using the layoutmanager
            int lastVisibleItemPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            return lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1;
        }
        return  false;
        }

    private void readFromFirebase() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Post");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                posts.clear();
                Log.d("Home",dataSnapshot.toString());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    // only show those post that user has not liked
                    if (post != null && !Snapshot.child(post.getPostId()).exists()) {
                        posts.add(post);
                        no_post.setVisibility(View.INVISIBLE);

                    }
                }
                if(posts.size() <= 0 ) {
                    no_post.setVisibility(View.VISIBLE);
                }

                postAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
    private void getLikedPost() {

        DatabaseReference likedrefer = FirebaseDatabase.getInstance().getReference("Users")
                .child(String.valueOf(new SessionManagement(getContext()).getId())).child("LikedPost");
        likedrefer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Snapshot = dataSnapshot;
                readFromFirebase();
                shimmerContainer.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                page++;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Home","error"+databaseError.toString());
            }
        });
    }


}