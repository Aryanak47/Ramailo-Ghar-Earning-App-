package com.aryanapps.ramailoghar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;


public class Splash extends AppCompatActivity {

    private final static int SPLASH_TIME = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // TODO to use splash screen to load data from firebase
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Splash.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME);
    }
}
