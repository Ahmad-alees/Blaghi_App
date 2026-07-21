package com.example.balaghi.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.balaghi.R;
import com.example.balaghi.adapters.ImageGridAdapter;
import com.example.balaghi.database.DatabaseHelper;
import com.example.balaghi.databinding.ActivityEditReportBinding;
import com.example.balaghi.models.Report;
import com.example.balaghi.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditReportActivity extends AppCompatActivity {

    private ActivityEditReportBinding binding;
    private DatabaseHelper databaseHelper;
    private Report report;

    private ImageGridAdapter imageAdapter;
    private final List<Uri> selectedImages = new ArrayList<>();
    private Uri cameraImageUri;

    private static final int MAX_IMAGES = 5;

    private static final String KEY_TITLE    = "er_title";
    private static final String KEY_DESC     = "er_desc";
    private static final String KEY_LOCATION = "er_loc";
    private static final String KEY_CAT_POS  = "er_cat";
    private static final String KEY_ANON     = "er_anon";
    private static final String KEY_IMAGES   = "er_images";

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedImages.add(uri);
                        imageAdapter.notifyDataSetChanged();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && cameraImageUri != null) {
                    selectedImages.add(cameraImageUri);
                    imageAdapter.notifyDataSetChanged();
                }
            });

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), granted -> {
                boolean allGranted = !granted.containsValue(false);
                if (allGranted) {
                    Toast.makeText(this, "تم منح الصلاحية — اضغط + مرة أخرى", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        report = (Report) getIntent().getSerializableExtra(Constants.EXTRA_REPORT);
        if (report == null) { finish(); return; }

        binding.tvTitle.setText("تعديل البلاغ");
        binding.btnSaveReport.setText("حفظ التعديلات");

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupSpinner();
        setupImageGrid();

        if (savedInstanceState != null) {
            binding.etTitle.setText(savedInstanceState.getString(KEY_TITLE, ""));
            binding.etDescription.setText(savedInstanceState.getString(KEY_DESC, ""));
            binding.etLocation.setText(savedInstanceState.getString(KEY_LOCATION, ""));
            binding.spinnerCategory.setSelection(savedInstanceState.getInt(KEY_CAT_POS, 0));
            binding.cbAnonymous.setChecked(savedInstanceState.getBoolean(KEY_ANON, false));

            ArrayList<String> savedImages = savedInstanceState.getStringArrayList(KEY_IMAGES);
            if (savedImages != null) {
                selectedImages.clear();
                for (String uriStr : savedImages) {
                    selectedImages.add(Uri.parse(uriStr));
                }
                imageAdapter.notifyDataSetChanged();
            }
        } else {
            prefillFields();
        }

        binding.tilLocation.setEndIconOnClickListener(v -> {
            String location = binding.etLocation.getText() != null
                    ? binding.etLocation.getText().toString().trim() : "";
            if (location.isEmpty()) {
                binding.etLocation.setError("أدخل اسم الموقع أولاً");
                return;
            }
            Uri geoUri = Uri.parse("geo:0,0?q=" + Uri.encode(location));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Uri browserUri = Uri.parse("https://maps.google.com/?q=" + Uri.encode(location));
                startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
            }
        });

        binding.btnSaveReport.setOnClickListener(v -> updateReport());
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_TITLE,
                binding.etTitle.getText() != null ? binding.etTitle.getText().toString() : "");
        outState.putString(KEY_DESC,
                binding.etDescription.getText() != null ? binding.etDescription.getText().toString() : "");
        outState.putString(KEY_LOCATION,
                binding.etLocation.getText() != null ? binding.etLocation.getText().toString() : "");
        outState.putInt(KEY_CAT_POS, binding.spinnerCategory.getSelectedItemPosition());
        outState.putBoolean(KEY_ANON, binding.cbAnonymous.isChecked());

        ArrayList<String> imageUris = new ArrayList<>();
        for (Uri uri : selectedImages) {
            imageUris.add(uri.toString());
        }
        outState.putStringArrayList(KEY_IMAGES, imageUris);
    }

    private void setupSpinner() {
        String[] rawCategories = getResources().getStringArray(R.array.categories_array);
        String[] categories = new String[rawCategories.length + 1];
        categories[0] = "اختر التصنيف";
        System.arraycopy(rawCategories, 0, categories, 1, rawCategories.length);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void setupImageGrid() {
        imageAdapter = new ImageGridAdapter(selectedImages, MAX_IMAGES, new ImageGridAdapter.Listener() {
            @Override
            public void onAddClick() {
                showImageSourceDialog();
            }
            @Override
            public void onRemoveClick(int position) {
                if (position >= 0 && position < selectedImages.size()) {
                    selectedImages.remove(position);
                    imageAdapter.notifyDataSetChanged();
                }
            }
        });
        binding.rvImages.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvImages.setAdapter(imageAdapter);
    }

    private void prefillFields() {
        binding.etTitle.setText(report.getTitle());
        binding.etDescription.setText(report.getDescription());
        binding.etLocation.setText(report.getLocation());
        binding.cbAnonymous.setChecked(report.isAnonymous());

        ArrayAdapter adapter = (ArrayAdapter) binding.spinnerCategory.getAdapter();
        if (adapter != null && report.getCategory() != null) {
            int catPos = adapter.getPosition(report.getCategory());
            if (catPos >= 0) binding.spinnerCategory.setSelection(catPos);
        }

        String existingImage = report.getImage();
        if (existingImage != null && !existingImage.trim().isEmpty()) {
            selectedImages.add(Uri.parse(existingImage.trim()));
            imageAdapter.notifyDataSetChanged();
        }
    }


    private void showImageSourceDialog() {
        if (selectedImages.size() >= MAX_IMAGES) {
            Toast.makeText(this, "الحد الأقصى " + MAX_IMAGES + " صور", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("إضافة صورة")
                .setItems(new String[]{"التقاط صورة بالكاميرا", "اختيار من المعرض"},
                        (dialog, which) -> {
                            if (which == 0) openCamera();
                            else openGallery();
                        })
                .show();
    }

    private void openCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
            return;
        }
        try {
            File photoFile = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", photoFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            Toast.makeText(this, "تعذّر فتح الكاميرا", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(new String[]{permission});
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("RPT_" + timeStamp, ".jpg", storageDir);
    }

    private void updateReport() {
        String title       = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String location     = binding.etLocation.getText().toString().trim();
        String category     = binding.spinnerCategory.getSelectedItem().toString();

        if (title.isEmpty())       { binding.etTitle.setError("أدخل عنوان البلاغ"); return; }
        if (description.isEmpty()) { binding.etDescription.setError("أدخل وصف البلاغ"); return; }
        if (location.isEmpty())    { binding.etLocation.setError("أدخل موقع البلاغ"); return; }
        if (category.equals("اختر التصنيف")) {
            Toast.makeText(this, "الرجاء اختيار تصنيف", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageUri = selectedImages.isEmpty() ? "" : selectedImages.get(0).toString();

        report.setTitle(title);
        report.setDescription(description);
        report.setCategory(category);
        report.setLocation(location);
        report.setImage(imageUri);
        report.setAnonymous(binding.cbAnonymous.isChecked());

        if (databaseHelper.updateReport(report)) {
            Toast.makeText(this, "تم تحديث البلاغ بنجاح", Toast.LENGTH_SHORT).show();
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