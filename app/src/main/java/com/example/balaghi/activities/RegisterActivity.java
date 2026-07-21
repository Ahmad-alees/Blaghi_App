package com.example.balaghi.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.balaghi.database.DatabaseHelper;
import com.example.balaghi.databinding.ActivityRegisterBinding;
import com.example.balaghi.models.User;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.etBirthDate.setOnClickListener(v -> showDatePicker());
        binding.btnRegister.setOnClickListener(v -> registerUser());
        binding.tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String fullName = binding.etFullName.getText().toString().trim();
        String username = binding.etUserName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhoneNum.getText().toString().trim();
        String birthDate = binding.etBirthDate.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (!validateInputs(fullName, username, email, phone, birthDate, password)) {
            return;
        }

        User user = new User();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setBirthDate(birthDate);
        user.setPassword(password);
        user.setProfileImage("");

        boolean isRegistered = databaseHelper.registerUser(user);
        if (isRegistered) {
            Toast.makeText(this, "تم إنشاء الحساب بنجاح", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "البريد الإلكتروني مستخدم مسبقًا", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs(String fullName, String username, String email,
                                   String phone, String birthDate, String password) {

        boolean isValid = true;

        if (fullName.isEmpty()) {
            binding.etFullName.setError("أدخل الاسم الكامل");
            isValid = false;
        }

        if (username.isEmpty()) {
            binding.etUserName.setError("أدخل اسم المستخدم");
            isValid = false;
        }

        if (email.isEmpty()) {
            binding.etEmail.setError("أدخل البريد الإلكتروني");
            isValid = false;
        } else if (!isValidGmail(email)) {
            binding.etEmail.setError("البريد الإلكتروني يجب أن يكون بصيغة example@gmail.com");
            isValid = false;
        }

        if (phone.isEmpty()) {
            binding.etPhoneNum.setError("أدخل رقم الهاتف");
            isValid = false;
        } else if (!isValidPhone(phone)) {
            binding.etPhoneNum.setError("رقم الهاتف يجب أن يتكون من 10 أرقام بالضبط");
            isValid = false;
        }

        if (birthDate.isEmpty()) {
            binding.etBirthDate.setError("اختر تاريخ الميلاد");
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

    private boolean isValidPhone(String phone) {

        if (phone.length() != 10) {
            return false;
        }

        for (int i = 0; i < phone.length(); i++) {
            char c = phone.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String date = day + "/" + (month + 1) + "/" + year;
                    binding.etBirthDate.setText(date);
                    binding.etBirthDate.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}