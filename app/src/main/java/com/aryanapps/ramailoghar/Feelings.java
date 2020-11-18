package com.aryanapps.ramailoghar;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.aryanapps.ramailoghar.helper.Network;
import com.aryanapps.ramailoghar.helper.Timer;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import nl.dionsegijn.konfetti.KonfettiView;
import ticker.views.com.ticker.widgets.circular.timer.view.CircularView;


public class Feelings extends AppCompatActivity {


    InterstitialAd mInterstitialAd;
    Button nextBtn;
    int[] imagesId = new int[34];
    ImageView imageView;
    Network network;
    CircularView circularViewWithTimer;
    Timer timer;
    KonfettiView konfettiView;
    Toolbar toolbar;
    AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feelings);

        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Watch Ads");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });

        nextBtn = findViewById(R.id.nextBtn);
        imageView = findViewById(R.id.felingImg);
        circularViewWithTimer = findViewById(R.id.circular_feeling);
        timer = new Timer(nextBtn, circularViewWithTimer);
        konfettiView = findViewById(R.id.viewKonfetti);
        network = new Network(getApplicationContext(), konfettiView);


        //get images from drawable folder 34
        for (int i = 0; i < imagesId.length; i++) {
            imagesId[i] = getResources().getIdentifier("drawable/" + "f" + i, null, getPackageName());
        }


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-9221359940930821/4192691020");
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                timer.pauseTime();
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                timer.playTime();
                network.updateAmount(true);
                loadAd();

            }
        });

        shownextImage();
        loadAd();
    }

    private void loadAd() {
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void shownextImage() {
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected(getApplicationContext())) {
                    timer.setTimer();
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    }
//                    counter = counter % imagesId.length;
                    int random = (int) (Math.random() * imagesId.length);
                    imageView.setImageResource(imagesId[random]);
//                    counter++;
                    return;
                }
                Intent intent = new Intent(Feelings.this, MainActivity.class);
                startActivity(intent);
                finish();

            }
        });
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


    @Override
    protected void onStop() {
        super.onStop();
        network.stopNetworking();
    }


}
