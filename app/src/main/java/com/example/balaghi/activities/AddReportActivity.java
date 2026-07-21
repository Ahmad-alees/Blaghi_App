package com.example.balaghi.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;

import com.example.balaghi.R;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.balaghi.adapters.ImageGridAdapter;
import com.example.balaghi.database.DatabaseHelper;
import com.example.balaghi.databinding.ActivityAddReportBinding;
import com.example.balaghi.models.Report;
import com.example.balaghi.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddReportActivity extends AppCompatActivity {

    private ActivityAddReportBinding binding;

    private static final int MAX_IMAGES = 6;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int LOCATION_PERMISSION_CODE = 101;
    private static final int CAMERA_PERMISSION_CODE_OCR = 102;

    private static final String KEY_SELECTED_IMAGES = "key_selected_images";
    private static final String KEY_CAMERA_PHOTO_URI = "key_camera_photo_uri";
    private static final String KEY_OCR_IMAGE_URI = "key_ocr_image_uri";
    private static final String KEY_OCR_CAMERA_PHOTO_URI = "key_ocr_camera_photo_uri";

    private final List<Uri> selectedImageUris = new ArrayList<>();
    private ImageGridAdapter imageAdapter;

    private Uri cameraPhotoUri;
    private DatabaseHelper dbHelper;

    private Uri ocrImageUri;
    private Uri ocrCameraPhotoUri;

    private TextRecognizer textRecognizer;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null
                        && result.getData().getData() != null) {
                    addImage(result.getData().getData());
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraPhotoUri != null) {
                    addImage(cameraPhotoUri);
                }
            });

    private final ActivityResultLauncher<Intent> ocrGalleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null
                        && result.getData().getData() != null) {
                    setOcrImage(result.getData().getData());
                }
            });

    private final ActivityResultLauncher<Uri> ocrCameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && ocrCameraPhotoUri != null) {
                    setOcrImage(ocrCameraPhotoUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAddReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);

        setupImageGrid();

        if (savedInstanceState != null) {
            List<Uri> restoredImages = savedInstanceState.getParcelableArrayList(KEY_SELECTED_IMAGES);
            if (restoredImages != null && !restoredImages.isEmpty()) {
                selectedImageUris.clear();
                selectedImageUris.addAll(restoredImages);
                imageAdapter.notifyDataSetChanged();
            }
            cameraPhotoUri = savedInstanceState.getParcelable(KEY_CAMERA_PHOTO_URI);
            ocrCameraPhotoUri = savedInstanceState.getParcelable(KEY_OCR_CAMERA_PHOTO_URI);

            Uri restoredOcrUri = savedInstanceState.getParcelable(KEY_OCR_IMAGE_URI);
            if (restoredOcrUri != null) {
                setOcrImage(restoredOcrUri);
            }
        }

        binding.btnPickOcrImage.setOnClickListener(v -> showOcrImageSourceDialog());

        binding.btnExtractText.setOnClickListener(v -> extractTextFromImage());

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

        binding.btnSaveReport.setOnClickListener(v -> saveReport());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_SELECTED_IMAGES, new ArrayList<>(selectedImageUris));
        if (cameraPhotoUri != null) {
            outState.putParcelable(KEY_CAMERA_PHOTO_URI, cameraPhotoUri);
        }
        if (ocrImageUri != null) {
            outState.putParcelable(KEY_OCR_IMAGE_URI, ocrImageUri);
        }
        if (ocrCameraPhotoUri != null) {
            outState.putParcelable(KEY_OCR_CAMERA_PHOTO_URI, ocrCameraPhotoUri);
        }
    }


    private void setupImageGrid() {
        binding.rvImages.setLayoutManager(new GridLayoutManager(this, 2));
        imageAdapter = new ImageGridAdapter(selectedImageUris, MAX_IMAGES, new ImageGridAdapter.Listener() {
            @Override
            public void onAddClick() {
                showImageSourceDialog();
            }

            @Override
            public void onRemoveClick(int position) {
                selectedImageUris.remove(position);
                imageAdapter.notifyDataSetChanged();
            }
        });
        binding.rvImages.setAdapter(imageAdapter);
    }

    private void addImage(Uri uri) {
        selectedImageUris.add(uri);
        imageAdapter.notifyDataSetChanged();
    }

    private void setOcrImage(Uri uri) {
        ocrImageUri = uri;
        binding.imgOcrPreview.setVisibility(android.view.View.VISIBLE);
        binding.imgOcrPreview.setImageURI(ocrImageUri);
        binding.btnExtractText.setEnabled(true);
    }

    private void showOcrImageSourceDialog() {
        String[] options = {"التقاط صورة بالكاميرا", "اختيار من المعرض"};
        new AlertDialog.Builder(this)
                .setTitle("اختر مصدر الصورة")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) checkCameraPermissionAndOpenForOcr();
                    else openOcrGallery();
                })
                .show();
    }

    private void openOcrGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        ocrGalleryLauncher.launch(intent);
    }

    private void checkCameraPermissionAndOpenForOcr() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE_OCR);
        } else {
            openCameraForOcr();
        }
    }

    private void openCameraForOcr() {
        File photoFile;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "حدث خطأ أثناء تجهيز الصورة", Toast.LENGTH_SHORT).show();
            return;
        }
        ocrCameraPhotoUri = FileProvider.getUriForFile(
                this, getApplicationContext().getPackageName() + ".fileprovider", photoFile);
        ocrCameraLauncher.launch(ocrCameraPhotoUri);
    }


    private void extractTextFromImage() {
        if (ocrImageUri == null) {
            Toast.makeText(this, "الرجاء اختيار صورة أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputImage image = InputImage.fromFilePath(this, ocrImageUri);

            binding.btnExtractText.setEnabled(false);
            binding.btnExtractText.setText("جاري الاستخراج...");

            textRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        binding.btnExtractText.setEnabled(true);
                        binding.btnExtractText.setText("استخراج النص");

                        String extracted = visionText.getText();
                        if (extracted.trim().isEmpty()) {
                            Toast.makeText(this,
                                    "لم يتم العثور على نص (المكتبة تدعم الإنجليزي فقط ولا تتعرف على العربي)",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            binding.etOcrResult.setText(extracted);
                        }
                    })
                    .addOnFailureListener(e -> {
                        binding.btnExtractText.setEnabled(true);
                        binding.btnExtractText.setText("استخراج النص");
                        Toast.makeText(this, "فشل استخراج النص: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (IOException e) {
            Toast.makeText(this, "تعذر قراءة الصورة المختارة", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImageSourceDialog() {
        String[] options = {"التقاط صورة بالكاميرا", "اختيار من المعرض"};
        new AlertDialog.Builder(this)
                .setTitle("اختر مصدر الصورة")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) checkCameraPermissionAndOpen();
                    else openGallery();
                })
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        File photoFile;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "حدث خطأ أثناء تجهيز الصورة", Toast.LENGTH_SHORT).show();
            return;
        }
        cameraPhotoUri = FileProvider.getUriForFile(
                this, getApplicationContext().getPackageName() + ".fileprovider", photoFile);
        cameraLauncher.launch(cameraPhotoUri);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir("Pictures");
        return File.createTempFile("REPORT_" + timeStamp, ".jpg", storageDir);
    }


    private void getCurrentLocationAndFill() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location lastKnown = null;
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (lastKnown == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "لا يمكن الوصول للموقع", Toast.LENGTH_SHORT).show();
            return;
        }

        if (lastKnown != null) {
            fillLocationFromCoordinates(lastKnown.getLatitude(), lastKnown.getLongitude());
        } else {
            requestFreshLocation(locationManager);
        }
    }

    private void requestFreshLocation(LocationManager locationManager) {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this, "الرجاء تفعيل خدمة الموقع (GPS)", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;

        Toast.makeText(this, "جاري تحديد الموقع...", Toast.LENGTH_SHORT).show();

        LocationListener singleLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                fillLocationFromCoordinates(loc.getLatitude(), loc.getLongitude());
            }

            @Override
            public void onProviderDisabled(String disabledProvider) {
            }

            @Override
            public void onProviderEnabled(String enabledProvider) {
            }

            @Override
            public void onStatusChanged(String changedProvider, int status, Bundle extras) {
            }
        };

        try {
            locationManager.requestSingleUpdate(selectedProvider, singleLocationListener, Looper.getMainLooper());
        } catch (SecurityException e) {
            Toast.makeText(this, "لا يمكن الوصول للموقع", Toast.LENGTH_SHORT).show();
        }
    }

    private void fillLocationFromCoordinates(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(this, new Locale("ar"));
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty() && addresses.get(0).getAddressLine(0) != null) {
                binding.etLocation.setText(addresses.get(0).getAddressLine(0));
            } else {
                binding.etLocation.setText(lat + ", " + lng);
            }
        } catch (IOException e) {
            binding.etLocation.setText(lat + ", " + lng);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            if (requestCode == CAMERA_PERMISSION_CODE || requestCode == CAMERA_PERMISSION_CODE_OCR) {
                Toast.makeText(this, "يجب السماح باستخدام الكاميرا", Toast.LENGTH_SHORT).show();
            } else if (requestCode == LOCATION_PERMISSION_CODE) {
                Toast.makeText(this, "يجب السماح بالوصول للموقع", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (requestCode == CAMERA_PERMISSION_CODE) openCamera();
        else if (requestCode == CAMERA_PERMISSION_CODE_OCR) openCameraForOcr();
        else if (requestCode == LOCATION_PERMISSION_CODE) getCurrentLocationAndFill();
    }


    private void saveReport() {
        String title = binding.etTitle.getText() == null ? "" : binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText() == null ? "" : binding.etDescription.getText().toString().trim();
        String location = binding.etLocation.getText() == null ? "" : binding.etLocation.getText().toString().trim();
        String category = binding.spinnerCategory.getSelectedItem() == null ? "" : binding.spinnerCategory.getSelectedItem().toString();
        boolean isAnonymous = binding.cbAnonymous.isChecked();

        if (title.isEmpty()) { binding.etTitle.setError("الرجاء إدخال عنوان البلاغ"); binding.etTitle.requestFocus(); return; }
        if (description.isEmpty()) { binding.etDescription.setError("الرجاء إدخال وصف المشكلة"); binding.etDescription.requestFocus(); return; }
        if (location.isEmpty()) { binding.etLocation.setError("الرجاء إدخال الموقع"); binding.etLocation.requestFocus(); return; }
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(this, "الرجاء إرفاق صورة واحدة على الأقل", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        int userId = prefs.getInt(Constants.KEY_USER_ID, -1);

        if (userId == -1) {
            Toast.makeText(this, "خطأ: لم يتم التعرف على المستخدم، سجّل الدخول من جديد", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        List<String> uriStrings = new ArrayList<>();
        for (Uri u : selectedImageUris) uriStrings.add(u.toString());
        String imagesJoined = TextUtils.join(",", uriStrings);

        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        Report report = new Report();
        report.setTitle(title);
        report.setDescription(description);
        report.setCategory(category);
        report.setStatus("قيد المعالجة");
        report.setLocation(location);
        report.setImage(imagesJoined);
        report.setDate(currentDate);
        report.setAnonymous(isAnonymous);
        report.setUserId(userId);

        long result = dbHelper.insertReport(report);
        if (result != -1) {
            report.setId((int) result);
            Toast.makeText(this, "تم إرسال البلاغ بنجاح", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "حدث خطأ أثناء إرسال البلاغ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textRecognizer != null) {
            textRecognizer.close();
        }
    }
}