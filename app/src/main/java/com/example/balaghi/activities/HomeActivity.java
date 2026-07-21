package com.example.balaghi.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.balaghi.adapters.ReportAdapter;
import com.example.balaghi.database.DatabaseHelper;
import com.example.balaghi.databinding.ActivityHomeBinding;
import com.example.balaghi.models.Report;
import com.example.balaghi.models.User;
import com.example.balaghi.utils.Constants;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements ReportAdapter.OnReportClickListener {

    private ActivityHomeBinding binding;
    private DatabaseHelper databaseHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        userId = prefs.getInt(Constants.KEY_USER_ID, -1);

        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.rvMyReports.setLayoutManager(new LinearLayoutManager(this));

        setupClickListeners();
        loadUserName();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStats();
        loadLatestReports();
    }

    private void setupClickListeners() {

        binding.fabAddReport.setOnClickListener(v ->
                startActivity(new Intent(this, AddReportActivity.class)));

        binding.navHome.setOnClickListener(v -> {
        });

        binding.navMyReports.setOnClickListener(v ->
                startActivity(new Intent(this, MyReportsActivity.class)));

        binding.navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void loadUserName() {
        User user = databaseHelper.getUserById(userId);
        if (user != null) {
            binding.tvWelcome.setText("مرحبًا، " + user.getFullName());
        }
    }

    private void refreshStats() {
        int rejected = databaseHelper.getReportCountByStatus(userId, "مرفوض");
        int pending  = databaseHelper.getReportCountByStatus(userId, "قيد المعالجة");
        int resolved = databaseHelper.getReportCountByStatus(userId, "تم الحل");

        binding.tvTotalCount.setText(String.valueOf(rejected));
        binding.tvPendingCount.setText(String.valueOf(pending));
        binding.tvResolvedCount.setText(String.valueOf(resolved));
    }

    private void loadLatestReports() {
        List<Report> latest = databaseHelper.getLatestReports(userId, 2);

        if (latest.isEmpty()) {
            binding.rvMyReports.setVisibility(View.GONE);
            binding.tvNoMyReports.setVisibility(View.VISIBLE);
        } else {
            binding.rvMyReports.setVisibility(View.VISIBLE);
            binding.tvNoMyReports.setVisibility(View.GONE);
            binding.rvMyReports.setAdapter(new ReportAdapter(latest, this));
        }
    }


    @Override
    public void onReportClick(Report report) {
        Intent intent = new Intent(this, ReportDetailsActivity.class);
        intent.putExtra(Constants.EXTRA_REPORT, report);
        startActivity(intent);
    }

    @Override
    public void onEditClick(Report report) {
        Intent intent = new Intent(this, EditReportActivity.class);
        intent.putExtra(Constants.EXTRA_REPORT, report);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Report report) {
        databaseHelper.deleteReport(report.getId());
        loadLatestReports();
        refreshStats();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}