package com.matt.firebasestorage.Splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.matt.firebasestorage.NavigationActivity;
import com.matt.firebasestorage.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        setUpHandler(NavigationActivity.class, 4000);
    }

    private void setUpHandler(final Class<?> targetActivity, int SPLASH_TIME_OUT) {
        if (targetActivity != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(getApplicationContext(), targetActivity);
                    startActivity(i);
                    // close splash
                    finish();
                }
            }, SPLASH_TIME_OUT);
        }
    }
}