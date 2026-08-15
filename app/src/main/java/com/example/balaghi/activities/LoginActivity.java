package com.example.balaghi.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.balaghi.database.DatabaseHelper;
import com.example.balaghi.databinding.ActivityLoginBinding;
import com.example.balaghi.models.User;
import com.example.balaghi.utils.Constants;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(Constants.KEY_REMEMBER_ME, false)) {
            String savedEmail = prefs.getString(Constants.KEY_SAVED_EMAIL, "");
            String savedPass  = prefs.getString(Constants.KEY_SAVED_PASSWORD, "");
            binding.etEmail.setText(savedEmail);
            binding.etPassword.setText(savedPass);
            binding.cbRememberMe.setChecked(true);
        }

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email    = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (!validateLoginInputs(email, password)) {
            return;
        }

        User user = databaseHelper.loginUser(email, password);
        if (user == null) {
            Toast.makeText(this, "البريد الإلكتروني أو كلمة المرور غير صحيحة", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean rememberMe = binding.cbRememberMe.isChecked();

        SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.putInt(Constants.KEY_USER_ID, user.getId());
        editor.putBoolean(Constants.KEY_REMEMBER_ME, rememberMe);

        if (rememberMe) {
            editor.putString(Constants.KEY_SAVED_EMAIL, email);
            editor.putString(Constants.KEY_SAVED_PASSWORD, password);
        } else {
            editor.remove(Constants.KEY_SAVED_EMAIL);
            editor.remove(Constants.KEY_SAVED_PASSWORD);
        }
        editor.apply();

        Toast.makeText(this, "مرحبًا " + user.getFullName(), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }

    private boolean validateLoginInputs(String email, String password) {

        boolean isValid = true;

        if (email.isEmpty()) {
            binding.etEmail.setError("أدخل البريد الإلكتروني");
            isValid = false;
        } else if (!isValidGmail(email)) {
            binding.etEmail.setError("صيغة البريد الإلكتروني غير صحيحة");
            isValid = false;
        }

        if (password.isEmpty()) {
            binding.etPassword.setError("أدخل كلمة المرور");
            isValid = false;
        } else if (password.length() < 8) {
            binding.etPassword.setError("كلمة المرور يجب أن تكون 8 أحرف على الأقل");
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidGmail(String email) {

        if (!email.contains("@")) {
            return false;
        }

        if (!email.endsWith("@gmail.com")) {
            return false;
        }

        int atIndex = email.indexOf("@");
        if (atIndex == 0) {
            return false;
        }

        if (email.indexOf("@") != email.lastIndexOf("@")) {
            return false;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}