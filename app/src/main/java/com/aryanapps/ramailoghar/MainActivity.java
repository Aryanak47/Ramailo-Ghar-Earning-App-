package com.aryanapps.ramailoghar;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.aryanapps.ramailoghar.fragment.EarningFragment;
import com.aryanapps.ramailoghar.fragment.HomeFragment;
import com.aryanapps.ramailoghar.fragment.NotificationFragment;
import com.aryanapps.ramailoghar.helper.Network;
import com.aryanapps.ramailoghar.model.User;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, RewardedVideoAdListener {

    SessionManagement sessionManagement;
    HashMap<String, String> userInfo;

    TextView firstLetter;
    LinearLayout userProfile;
    TextView userEmail;
    TextView userName;

    Fragment selectedFragment;
    BottomNavigationView bottomNavigationView;

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    ImageButton editBtn;
    Thread thread;

    Dialog net_dialog;
    Button net_okBtn;
    TextView net_title;
    TextView net_msg;
    RewardedVideoAd rewardedAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);


//        AdRequest adRequest1 = new AdRequest.Builder().build();
//        adView.loadAd(adRequest1);
//        adView.setAdListener(new AdListener(){
//            @Override
//            public void onAdFailedToLoad(int i) {
//              adView.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAdLoaded() {
//                adView.setVisibility(View.VISIBLE);
//
//            }
//        });


        sessionManagement = new SessionManagement(getApplicationContext());
        userInfo = sessionManagement.getUser();
        Toast.makeText(getApplicationContext(), "Welcome " + userInfo.get(sessionManagement.NAME), Toast.LENGTH_SHORT).show();

        navigationView = findViewById(R.id.navigation);
        setUserInfo();

        net_dialog = new Dialog(this);
        net_dialog.setContentView(R.layout.internet_dialog);
        net_msg = net_dialog.findViewById(R.id.netMsgDialog);
        net_title = net_dialog.findViewById(R.id.netTitleDialog);
        net_okBtn = net_dialog.findViewById(R.id.netDialogBtn);
        net_dialog.setCancelable(false);
        net_dialog.setCanceledOnTouchOutside(false);

        drawerLayout = findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolbar);
        //setting toolbar
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.opendrawer, R.string.closedrawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView = findViewById(R.id.navigation_bottom);
        setListenerOnNavigation();
        setNotificationBadge();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();

        keepTrackOfILiked();
        keepTrackOfUserPostLike();
        reload();
        totalPostILiked();
        rewardedAd = MobileAds.getRewardedVideoAdInstance(this);
        rewardedAd.setRewardedVideoAdListener(this);
        loadVideoAds();
        loadRewardedVideoAd();

    }

    private void setNotificationBadge() {
        BottomNavigationMenuView bottomNavigationView = (BottomNavigationMenuView) this.bottomNavigationView.getChildAt(0);
        View v = bottomNavigationView.getChildAt(3);
        BottomNavigationItemView bottomNavigationItemView = (BottomNavigationItemView) v;
        View badge = LayoutInflater.from(getApplicationContext()).inflate(R.layout.notification_badge, bottomNavigationItemView, true);
        final TextView notificationCounter = badge.findViewById(R.id.notifications_badge);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Notification")
                .child(String.valueOf(sessionManagement.getId()));
        databaseReference.orderByChild("isSeen").equalTo(false).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                if (count == 0) {
                    notificationCounter.setVisibility(View.GONE);
                    return;
                }
                notificationCounter.setVisibility(View.VISIBLE);
                if (count > 9) {
                    notificationCounter.setText("9+");
                } else {
                    notificationCounter.setText(String.valueOf(count));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setListenerOnNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        selectedFragment = new HomeFragment();
                        break;

                    case R.id.nav_withdraw:
                        selectedFragment = null;
                        startActivity(new Intent(MainActivity.this, WithDraw.class));

                        break;

                    case R.id.notification:
                        selectedFragment = new NotificationFragment();
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Notification")
                                .child(String.valueOf(new SessionManagement(getApplicationContext()).getId()));
                        databaseReference.orderByChild("isSeen").equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        snapshot.getRef().child("isSeen").setValue(true);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        break;

                    case R.id.earn_money:
                        selectedFragment = new EarningFragment();
                        break;
                    case R.id.addpost:
                        selectedFragment = null;
                        startActivity(new Intent(MainActivity.this, PostActivity.class));
                        break;
                }
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                }
                return true;
            }
        });
    }

    private void setUserInfo() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(String.valueOf(sessionManagement.getId()));
        View view = navigationView.getHeaderView(0);
        editBtn = view.findViewById(R.id.edit);
        firstLetter = navigationView.getHeaderView(0).findViewById(R.id.first_letter);
        userName = navigationView.getHeaderView(0).findViewById(R.id.user_name);
        userEmail = navigationView.getHeaderView(0).findViewById(R.id.userGmail);
        userProfile = navigationView.getHeaderView(0).findViewById(R.id.profile_image);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    sessionManagement.setName(user.getUserName());
                    sessionManagement.setEmail(user.getEmail());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        userEmail.setText(sessionManagement.getEmail());
        userName.setText(sessionManagement.getName());
        String letter = sessionManagement.getName().substring(0, 1);
        firstLetter.setText(letter);
        int color = generateColor();
        GradientDrawable drawable = (GradientDrawable) userProfile.getBackground();
        drawable.setColor(color);

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });
    }

    private int generateColor() {
        String[] colors = {"#e84118", "#7f8fa6", "#44bd32", "#353b48"};
        int random_index = (int) (Math.random() * colors.length);
        return Color.parseColor(colors[random_index]);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menus, menu);
        return true;
    }

//            @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.send_msg:
//              Toast.makeText(getApplicationContext(),"Send Messages",Toast.LENGTH_LONG).show();
//                return false;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    // navigation items
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.withdraw) {
            Intent intent = new Intent(MainActivity.this, WithDraw.class);
            startActivity(intent);

        } else if (menuItem.getItemId() == R.id.logout) {
            sessionManagement.logOut();
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        } else if (menuItem.getItemId() == R.id.invite) {
            Intent sharingIntents = new Intent(Intent.ACTION_SEND);
            sharingIntents.setType("text/plain");
            String shareBody = getResources().getString(R.string.app_desc) +
                    "\n🎁It's really good app that really pays you off.By simple task, you can earn money.🥳" +
                    "\n\n https://play.google.com/store/apps/details?id=com.aryanapps.ramailoghar";
            String shareSubject = "Share and earn money";
            sharingIntents.putExtra(Intent.EXTRA_TEXT, shareBody);
            sharingIntents.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
            startActivity(Intent.createChooser(sharingIntents, "Share Using"));
        } else if (menuItem.getItemId() == R.id.rate) {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (Exception e) {
                uri = Uri.parse("https://play.google.com/store/apps/details?id=com.aryanapps.ramailoghar");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }

        } else if (menuItem.getItemId() == R.id.fb) {
            String url = "https://www.facebook.com/Ramailo-Ghar-110618447426505/";
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    private void keepTrackOfILiked() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(String.valueOf(sessionManagement.getId()));
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    int totalILiked = user.getTotalPostILiked();
                    if (totalILiked >= user.getTotalPostILikedTarget()) {
                        // reward user
                        rewardUser(0.50f);
                        // update target in firebase
                        databaseReference.child("totalPostILikedTarget").setValue(user.getTotalPostILikedTarget() + 100);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void keepTrackOfUserPostLike() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(String.valueOf(sessionManagement.getId()));
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    int totalPostLike = user.getTotalPostLike();
                    if (totalPostLike >= user.getTotalPostLikeTarget()) {
                        // reward user
                        rewardUser(0.50f);
                        // update target in firebase
                        databaseReference.child("totalPostLikeTarget").setValue(user.getTotalPostLikeTarget() + 100);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void rewardUser(float amount) {
        new Network(getApplicationContext()).rewardUser(amount);
    }

    private void reload() {

        thread = new Thread() {
            @Override
            public void run() {
                while (!thread.isInterrupted()) {
                    try {
                        Thread.sleep(2000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!isConnected(getApplicationContext())) {
                                    net_dialog.show();
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        };
        thread.start();
    }

    private boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            return (mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting());
        } else
            return false;
    }

    public void close(View v) {
        finish();
    }

    private void totalPostILiked() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(String.valueOf(sessionManagement.getId()));
        DatabaseReference likedReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(String.valueOf(sessionManagement.getId()))
                .child("LikedPost");
        likedReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reference.child("totalPostILiked").setValue(dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadVideoAds() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(String.valueOf(sessionManagement.getId()));
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    int totalLiked = user.getTotalPostILiked();
                    if (totalLiked >= user.getVideoAdsTarget()) {
                        // load video ads
                        startVideoAds();
                        // update video ads target
                        reference.child("videoAdsTarget").setValue(user.getVideoAdsTarget() + 13);
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // video ads

    private void loadRewardedVideoAd() {
        rewardedAd.loadAd("ca-app-pub-9221359940930821/7784683180",
                new AdRequest.Builder().build());
    }

    private void startVideoAds() {
        if (rewardedAd.isLoaded()) {
            rewardedAd.show();
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {


    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();
        new Network(getApplicationContext()).updateAmount(false);

    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {


    }

    @Override
    public void onRewardedVideoCompleted() {


    }


}