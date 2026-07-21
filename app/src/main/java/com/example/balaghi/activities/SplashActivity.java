package com.example.balaghi.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.balaghi.databinding.ActivitySplashBinding;
import com.example.balaghi.utils.Constants;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

            boolean isLoggedIn  = prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
            boolean rememberMe  = prefs.getBoolean(Constants.KEY_REMEMBER_ME, false);

            Intent intent = (isLoggedIn && rememberMe)
                    ? new Intent(SplashActivity.this, HomeActivity.class)
                    : new Intent(SplashActivity.this, LoginActivity.class);

            startActivity(intent);
            finish();
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
