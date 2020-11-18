package com.aryanapps.ramailoghar;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.aryanapps.ramailoghar.helper.YoutubeConfig;
import com.aryanapps.ramailoghar.model.Video;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;
import ticker.views.com.ticker.widgets.circular.timer.callbacks.CircularViewCallback;
import ticker.views.com.ticker.widgets.circular.timer.view.CircularView;

public class Comedy_Videos extends YouTubeBaseActivity {


    List<String> links;
    YouTubePlayerView videoView;
    FloatingActionButton nxtBtn;
    FloatingActionButton prevBtn;
    CircularView circularViewWithTimer;
    YouTubePlayer player;
    SessionManagement sessionManagement;
    RequestQueue queue;
    KonfettiView konfettiView;
    MediaPlayer mediaPlayer;
    String videoId;
    TextView novideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comedy_videos);
        novideo = findViewById(R.id.novideo);
        this.sessionManagement = new SessionManagement(getApplicationContext());
        this.queue = Volley.newRequestQueue(getApplicationContext());
        konfettiView = findViewById(R.id.video_viewKonfetti);
        videoView = findViewById(R.id.videoView);
        nxtBtn = findViewById(R.id.nxtPlay);
        prevBtn = findViewById(R.id.prevBtn);
        changeBtnState(nxtBtn, false);
        changeBtnState(prevBtn, false);
        circularViewWithTimer = findViewById(R.id.circular_view);
        player = null;
        links = new ArrayList<>();
        getLinks();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.music);

        loadVideos();
        playNext();
        playPrev();


    }

    private void getLinks() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("YT Video");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Video video = snapshot.getValue(Video.class);
                        String temp = video.getUrl();
                        String url = temp.substring(temp.lastIndexOf("/") + 1);
//                        Toast.makeText(Comedy_Videos.this, url, Toast.LENGTH_SHORT).show();
                        if (!snapshot.child("seen").hasChild(String.valueOf(sessionManagement.getId()))) {
                            links.add(url);
                        }
                    }
                }
                if (links.size() == 0) {
                    novideo.setVisibility(View.VISIBLE);
                } else {
                    novideo.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void playNext() {
        nxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player != null) {
                    if (player.hasNext()) {
                        player.next();
                        changeBtnState(nxtBtn, false);
                        setTimer(player.getDurationMillis());
                    } else {
                        nxtBtn.setEnabled(false);
                        changeBtnBackGround(nxtBtn);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_video_txt), Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                changeBtnState(nxtBtn, false);
            }
        });

    }


    public void playPrev() {
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player != null) {
                    if (player.hasPrevious()) {
                        player.previous();
                        prevBtn.setEnabled(false);
                        changeBtnBackGround(prevBtn);
                        setTimer(player.getDurationMillis());
                    } else {
                        changeBtnState(prevBtn, false);
                        Toast.makeText(getApplicationContext(), "No more videos to play previous", Toast.LENGTH_SHORT).show();
                    }
                }
                changeBtnState(prevBtn, false);
            }
        });

    }

    private void setTimer(int durationMillis) {
        int time = Math.min((durationMillis / 1000), 180);
        CircularView.OptionsBuilder builderWithTimer =
                new CircularView.OptionsBuilder()
                        .shouldDisplayText(true)
                        .setCounterInSeconds(time)
                        .setCircularViewCallback(new CircularViewCallback() {
                            @Override
                            public void onTimerFinish() {
                                if (isConnected(getApplicationContext())) {
                                    doNetworking();
                                    if (!nxtBtn.isEnabled()) {
                                        changeBtnState(nxtBtn, true);
                                    }
                                    if (!prevBtn.isEnabled()) {
                                        changeBtnState(prevBtn, true);
                                    }
                                    if (player.hasNext()) {
                                        player.next();
                                    }
                                    return;
                                }
                                Intent intent = new Intent(Comedy_Videos.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onTimerCancelled() {
                            }
                        });

        circularViewWithTimer.setOptions(builderWithTimer);
        circularViewWithTimer.startTimer();
    }

    private void doNetworking() {
        final String URL = "https://ramaeloghar.herokuapp.com/bonus";
        int id = sessionManagement.getId();
        JSONObject params = new JSONObject();
        try {
            params.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest rq = new JsonObjectRequest(Request.Method.PUT, URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    mediaPlayer.start();
                    String amount = response.getString("amount");
                    sessionManagement.setAMOUNT(amount);
                    loadParticles();


                } catch (JSONException e) {
                    e.printStackTrace();
//                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        queue.add(rq);

    }

    private void changeBtnState(FloatingActionButton btn, boolean enable) {
        btn.setEnabled(enable);
        changeBtnBackGround(btn);
    }

    private void changeBtnBackGround(FloatingActionButton btn) {
        if (!btn.isEnabled()) {
            btn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.disabled)));
            return;
        }
        btn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.tapPropt)));

    }

    private void loadParticles() {
        konfettiView.build()
                .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .addSizes(new Size(12, 5f))
                .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                .streamFor(300, 5000L);

    }

    private void loadVideos() {
        videoView.initialize(YoutubeConfig.getApi(), new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(final YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                player = youTubePlayer;
                player.loadVideos(links);
                player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                    @Override
                    public void onLoading() {
                        Toast.makeText(Comedy_Videos.this, "loading", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onLoaded(String s) {
                        videoId = s;
                        setTimer(player.getDurationMillis());


                    }

                    @Override
                    public void onAdStarted() {

                    }

                    @Override
                    public void onVideoStarted() {


                    }

                    @Override
                    public void onVideoEnded() {
                        incrementViews(videoId);
                    }

                    @Override
                    public void onError(YouTubePlayer.ErrorReason errorReason) {
                        Toast.makeText(Comedy_Videos.this, "Something went wrong try again later", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.youtube_error), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void incrementViews(String s) {
        final String url = "https://youtu.be/" + s;
//        Log.d("Video",url);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("YT Video");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Video video = snapshot.getValue(Video.class);
//                    Log.d("Video",snapshot.toString());
                    if (video != null && video.getUrl().equals(url)) {
                        updateFireBase(video, video.getId(), video.getCurrentView(), video.getMaxView());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateFireBase(Video video, String id, int currentView, int maxView) {
        // add user to  seen member
        DatabaseReference seenReference = FirebaseDatabase.getInstance().getReference("YT Video")
                .child(id).child("seen");
        seenReference.child(String.valueOf(new SessionManagement(getApplicationContext()).getId())).setValue(true);
        //check whether it has exceeded max view or not i
        int newView = currentView + 1;
        if (newView > maxView) {
            addToCompletedVideoRequest(video);
            return;
        }

        HashMap<String, Object> newValue = new HashMap<>();
        newValue.put("currentView", newView);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("YT Video")
                .child(id);
        databaseReference.updateChildren(newValue);
    }

    private void addToCompletedVideoRequest(final Video video) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Completed Video Request");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("url", video.getUrl());
        hashMap.put("id", video.getId());
        hashMap.put("amount", video.getAmount());
        hashMap.put("maxView", video.getMaxView());
        hashMap.put("currentView", video.getCurrentView());
        hashMap.put("userid", video.getUserid());
        databaseReference.child(video.getId()).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    removeFromFireBase(video.getId());
                }
            }
        });
    }

    private void removeFromFireBase(String videoId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("YT Video")
                .child(videoId);
        databaseReference.removeValue();
    }

    //this method checks whether the device connected or !=connected on the internet...
    private boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = null;
        if (cm != null) {
            netinfo = cm.getActiveNetworkInfo();
        }

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            return (mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting());
        } else
            return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
        queue.stop();
    }
}
