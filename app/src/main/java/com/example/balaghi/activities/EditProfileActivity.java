package com.example.balaghi.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.balaghi.database.DatabaseHelper;
import com.example.balaghi.databinding.ActivityEditProfileBinding;
import com.example.balaghi.models.User;
import com.example.balaghi.utils.Constants;

import java.util.Calendar;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private DatabaseHelper databaseHelper;
    private User currentUser;
    private int userId;

    private static final String KEY_FULLNAME  = "ep_fullname";
    private static final String KEY_USERNAME  = "ep_username";
    private static final String KEY_EMAIL     = "ep_email";
    private static final String KEY_PHONE     = "ep_phone";
    private static final String KEY_BIRTHDATE = "ep_birthdate";
    private static final String KEY_NEW_PASS  = "ep_new_pass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        userId = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                .getInt(Constants.KEY_USER_ID, -1);

        currentUser = databaseHelper.getUserById(userId);
        if (currentUser == null) { finish(); return; }

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.etEditBirthDate.setOnClickListener(v -> showDatePicker());
        binding.etEditBirthDate.setFocusable(false);

        if (savedInstanceState != null) {
            binding.etEditFullName.setText(savedInstanceState.getString(KEY_FULLNAME, ""));
            binding.etEditUsername.setText(savedInstanceState.getString(KEY_USERNAME, ""));
            binding.etEditEmail.setText(savedInstanceState.getString(KEY_EMAIL, ""));
            binding.etEditPhone.setText(savedInstanceState.getString(KEY_PHONE, ""));
            binding.etEditBirthDate.setText(savedInstanceState.getString(KEY_BIRTHDATE, ""));
            binding.etEditNewPassword.setText(savedInstanceState.getString(KEY_NEW_PASS, ""));
        } else {
            prefillFields();
        }

        binding.btnSaveProfile.setOnClickListener(v -> saveProfile());
        binding.btnCancelProfile.setOnClickListener(v -> finish());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_FULLNAME,
                binding.etEditFullName.getText() != null ? binding.etEditFullName.getText().toString() : "");
        outState.putString(KEY_USERNAME,
                binding.etEditUsername.getText() != null ? binding.etEditUsername.getText().toString() : "");
        outState.putString(KEY_EMAIL,
                binding.etEditEmail.getText() != null ? binding.etEditEmail.getText().toString() : "");
        outState.putString(KEY_PHONE,
                binding.etEditPhone.getText() != null ? binding.etEditPhone.getText().toString() : "");
        outState.putString(KEY_BIRTHDATE,
                binding.etEditBirthDate.getText() != null ? binding.etEditBirthDate.getText().toString() : "");
        outState.putString(KEY_NEW_PASS,
                binding.etEditNewPassword.getText() != null ? binding.etEditNewPassword.getText().toString() : "");
    }

    private void prefillFields() {
        binding.etEditFullName.setText(currentUser.getFullName());
        binding.etEditUsername.setText(currentUser.getUsername());
        binding.etEditEmail.setText(currentUser.getEmail());
        binding.etEditPhone.setText(currentUser.getPhone());
        binding.etEditBirthDate.setText(currentUser.getBirthDate());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, day) ->
                        binding.etEditBirthDate.setText(day + "/" + (month + 1) + "/" + year),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveProfile() {
        String fullName   = binding.etEditFullName.getText().toString().trim();
        String username   = binding.etEditUsername.getText().toString().trim();
        String email      = binding.etEditEmail.getText().toString().trim();
        String phone      = binding.etEditPhone.getText().toString().trim();
        String birthDate  = binding.etEditBirthDate.getText().toString().trim();
        String newPass    = binding.etEditNewPassword.getText().toString().trim();


        if (fullName.isEmpty()) {
            binding.etEditFullName.setError("أدخل الاسم الكامل"); return;
        }
        if (username.isEmpty()) {
            binding.etEditUsername.setError("أدخل اسم المستخدم"); return;
        }
        if (email.isEmpty()) {
            binding.etEditEmail.setError("أدخل البريد الإلكتروني"); return;
        }

        if (!newPass.isEmpty()) {
            if (newPass.length() < 6) {
                binding.etEditNewPassword.setError("كلمة المرور يجب أن تكون 6 أحرف على الأقل"); return;
            }

            currentUser.setPassword(newPass);
        }

        currentUser.setFullName(fullName);
        currentUser.setUsername(username);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);
        currentUser.setBirthDate(birthDate);

        if (databaseHelper.updateUser(currentUser)) {
            Toast.makeText(this, "تم تحديث الملف الشخصي بنجاح", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "حدث خطأ أثناء التحديث", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
