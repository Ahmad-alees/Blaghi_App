package com.example.balaghi.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.balaghi.R;
import com.example.balaghi.adapters.ReportImagesViewAdapter;
import com.example.balaghi.database.DatabaseHelper;
import com.example.balaghi.databinding.ActivityReportDetailsBinding;
import com.example.balaghi.models.Report;
import com.example.balaghi.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ReportDetailsActivity extends AppCompatActivity {

    private ActivityReportDetailsBinding binding;
    private DatabaseHelper databaseHelper;
    private Report report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityReportDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        report = (Report) getIntent().getSerializableExtra(Constants.EXTRA_REPORT);
        if (report == null) {
            finish();
            return;
        }

        displayReport();
        displayImages();
        displayExtractedText();
        setupButtons();
    }

    private void displayReport() {
        binding.tvDetailTitle.setText(report.getTitle());
        binding.tvDetailDescription.setText(report.getDescription());
        binding.tvDetailCategory.setText(report.getCategory());
        binding.tvDetailLocation.setText(report.getLocation());
        binding.tvDetailDate.setText(report.getDate());
        binding.tvDetailAnonymous.setText(report.isAnonymous() ? "نعم (مجهول الهوية)" : "لا");
        binding.chipDetailStatus.setText(report.getStatus());
        binding.tvDetailReportId.setText("رقم البلاغ: #" + report.getId());

        switch (report.getStatus()) {
            case "تم الحل":
                binding.chipDetailStatus.setChipBackgroundColorResource(R.color.status_green);
                break;
            case "قيد المعالجة":
                binding.chipDetailStatus.setChipBackgroundColorResource(R.color.status_orange);
                break;
            case "مرفوض":
                binding.chipDetailStatus.setChipBackgroundColorResource(R.color.status_red);
                break;
            default:
                binding.chipDetailStatus.setChipBackgroundColorResource(R.color.status_blue);
                break;
        }
    }

    private void displayImages() {
        List<Uri> imageUris = new ArrayList<>();
        String imagesRaw = report.getImage();

        if (!TextUtils.isEmpty(imagesRaw)) {
            String[] parts = imagesRaw.split(",");
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    imageUris.add(Uri.parse(part.trim()));
                }
            }
        }

        if (imageUris.isEmpty()) {
            binding.rvDetailImages.setVisibility(android.view.View.GONE);
        } else {
            binding.rvDetailImages.setVisibility(android.view.View.VISIBLE);
            binding.rvDetailImages.setLayoutManager(
                    new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
            binding.rvDetailImages.setAdapter(new ReportImagesViewAdapter(imageUris));
        }
    }


    private void displayExtractedText() {
        String extractedText = report.getExtractedText();
        if (TextUtils.isEmpty(extractedText)) {
            binding.cardExtractedText.setVisibility(android.view.View.GONE);
        } else {
            binding.cardExtractedText.setVisibility(android.view.View.VISIBLE);
            binding.tvDetailExtractedText.setText(extractedText);
        }
    }

    private void setupButtons() {
        binding.btnBackDetail.setOnClickListener(v -> finish());

        binding.btnOpenMaps.setOnClickListener(v -> {
            String location = report.getLocation();
            Uri mapsUri = Uri.parse("geo:0,0?q=" + Uri.encode(location));
            Intent mapsIntent = new Intent(Intent.ACTION_VIEW, mapsUri);
            mapsIntent.setPackage("com.google.android.apps.maps");
            if (mapsIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapsIntent);
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://maps.google.com/?q=" + Uri.encode(location))));
            }
        });

        binding.btnEditDetail.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditReportActivity.class);
            intent.putExtra(Constants.EXTRA_REPORT, report);
            startActivity(intent);
            finish();
        });

        binding.btnDeleteDetail.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("حذف البلاغ")
                        .setMessage("هل أنت متأكد من حذف هذا البلاغ؟ لا يمكن التراجع عن هذا الإجراء.")
                        .setPositiveButton("حذف", (dialog, which) -> {
                            if (databaseHelper.deleteReport(report.getId())) {
                                Toast.makeText(this, "تم حذف البلاغ", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(this, "حدث خطأ أثناء الحذف", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("إلغاء", null)
                        .show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}