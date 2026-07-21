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
import com.example.balaghi.databinding.ActivityMyReportsBinding;
import com.example.balaghi.models.Report;
import com.example.balaghi.utils.Constants;

import java.util.List;

public class MyReportsActivity extends AppCompatActivity implements ReportAdapter.OnReportClickListener {

    private ActivityMyReportsBinding binding;
    private ReportAdapter adapter;
    private DatabaseHelper databaseHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMyReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        userId = prefs.getInt(Constants.KEY_USER_ID, -1);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.recyclerViewReports.setLayoutManager(new LinearLayoutManager(this));
        binding.btnBack.setOnClickListener(v -> finish());
        binding.fabAddReport.setOnClickListener(v ->
                startActivity(new Intent(this, AddReportActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReports();
    }

    private void loadReports() {
        List<Report> reports = databaseHelper.getAllReports(userId);
        if (reports.isEmpty()) {
            binding.recyclerViewReports.setVisibility(View.GONE);
            binding.tvEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewReports.setVisibility(View.VISIBLE);
            binding.tvEmpty.setVisibility(View.GONE);
            adapter = new ReportAdapter(reports, this);
            binding.recyclerViewReports.setAdapter(adapter);
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
        loadReports();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
