package com.app.nacfun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by Ardhitya Wiedha Irawan on 9/14/2017.
 */

public class Splashscreen extends Activity{
    private static int SPLASH_TIMER = 3500;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> {
            Intent i = new Intent(Splashscreen.this,MainActivity.class);
            startActivity(i);
            finish();
        },SPLASH_TIMER);
    }
}
