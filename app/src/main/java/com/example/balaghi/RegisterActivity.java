package com.example.balaghi;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.app.DatePickerDialog;
import java.util.Calendar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.balaghi.database.DatabaseHelper;
import com.example.balaghi.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etFullName;
    private TextInputEditText etUsername;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etBirthDate;
    private TextInputEditText etPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;

    private DatabaseHelper databaseHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        databaseHelper = new DatabaseHelper(this);
        tvLogin = findViewById(R.id.tvLoginLink);
        if (tvLogin == null) {
            throw new RuntimeException("tvLogin is NULL");
        }
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUserName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhoneNum);
        etBirthDate = findViewById(R.id.etBirthDate);
        etBirthDate.setOnClickListener(v -> showDatePicker());
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);


        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });


    }
    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (fullName.isEmpty()) {
            etFullName.setError("أدخل الاسم الكامل");
            return;
        }

        if (username.isEmpty()) {
            etUsername.setError("أدخل اسم المستخدم");
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("أدخل البريد الإلكتروني");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("أدخل كلمة المرور");
            return;
        }

        User user = new User();

        user.setFullName(fullName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setBirthDate(birthDate);
        user.setPassword(password);

// لا يوجد صورة شخصية عند التسجيل
        user.setProfileImage("");
        boolean isRegistered = databaseHelper.registerUser(user);
        if (isRegistered) {

            Toast.makeText(this,
                    "تم إنشاء الحساب بنجاح",
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        } else {

            Toast.makeText(this,
                    "البريد الإلكتروني مستخدم مسبقًا",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {

                    String date = selectedDay + "/"
                            + (selectedMonth + 1) + "/"
                            + selectedYear;

                    etBirthDate.setText(date);

                },
                year,
                month,
                day
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
}