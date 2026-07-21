package com.example.balaghi.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;

import android.widget.Toast;

import com.example.balaghi.R;
import com.example.balaghi.database.DatabaseHelper;
import com.example.balaghi.databinding.ActivityProfileBinding;
import com.example.balaghi.models.User;
import com.example.balaghi.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private DatabaseHelper databaseHelper;
    private int userId;
    private Uri cameraImageUri;


    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) saveProfileImage(uri);
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && cameraImageUri != null) {
                    saveProfileImage(cameraImageUri);
                }
            });

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), granted -> {
                if (!granted.containsValue(false)) {
                    Toast.makeText(this, "تم منح الصلاحية — اضغط على الصورة مرة أخرى", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        userId = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                .getInt(Constants.KEY_USER_ID, -1);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.cardLogout.setOnClickListener(v -> logout());
        binding.btnBackProfile.setOnClickListener(v -> finish());
        binding.btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        binding.frameAvatar.setOnClickListener(v -> showImageSourceDialog());
        binding.ivCameraIcon.setOnClickListener(v -> showImageSourceDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }


    private void loadUserData() {
        User user = databaseHelper.getUserById(userId);
        if (user == null) return;

        binding.tvProfileName.setText(user.getFullName());
        binding.tvProfileUsername.setText("@" + user.getUsername());
        binding.tvProfileEmail.setText(user.getEmail());

        String phone = user.getPhone();
        binding.tvProfilePhone.setText(
                (phone != null && !phone.isEmpty()) ? phone : "غير محدد");

        String birthDate = user.getBirthDate();
        binding.tvProfileBirthDate.setText(
                (birthDate != null && !birthDate.isEmpty()) ? birthDate : "غير محددة");

        String profileImagePath = user.getProfileImage();
        if (profileImagePath != null && !profileImagePath.isEmpty()) {
            try {
                ImageViewCompat.setImageTintList(binding.ivProfileAvatar, null);
                binding.ivProfileAvatar.setPadding(0, 0, 0, 0);
                File imgFile = new File(profileImagePath);
                if (imgFile.exists()) {
                    binding.ivProfileAvatar.setImageURI(Uri.fromFile(imgFile));
                } else {
                    setDefaultAvatar();
                }
            } catch (Exception e) {
                setDefaultAvatar();
            }
        } else {
            setDefaultAvatar();
        }

        binding.tvProfilePhone.setOnClickListener(v -> {
            if (phone != null && !phone.isEmpty()) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
            }
        });

        binding.tvProfileEmail.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + user.getEmail()));
            startActivity(Intent.createChooser(emailIntent, "إرسال بريد إلكتروني"));
        });
    }

    private void setDefaultAvatar() {
        binding.ivProfileAvatar.setImageResource(R.drawable.ic_person);
        binding.ivProfileAvatar.setPadding(
                dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20));
        ImageViewCompat.setImageTintList(binding.ivProfileAvatar,
                android.content.res.ColorStateList.valueOf(
                        getColor(R.color.white)));
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void showImageSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("صورة الملف الشخصي")
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
        return File.createTempFile("PRF_" + timeStamp, ".jpg", storageDir);
    }

    private void saveProfileImage(Uri sourceUri) {
        User user = databaseHelper.getUserById(userId);
        if (user == null) return;

        File destDir = new File(getFilesDir(), "avatars");
        if (!destDir.exists()) destDir.mkdirs();

        String fileName = "avatar_" + userId + "_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date()) + ".jpg";
        File destFile = new File(destDir, fileName);

        try (InputStream in  = getContentResolver().openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(destFile)) {

            if (in == null) {
                Toast.makeText(this, "تعذّر قراءة الصورة", Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Toast.makeText(this, "فشل حفظ الصورة: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        String oldPath = user.getProfileImage();
        if (oldPath != null && oldPath.startsWith("/")) {
            File oldFile = new File(oldPath);
            if (oldFile.exists()) oldFile.delete();
        }

        user.setProfileImage(destFile.getAbsolutePath());
        if (databaseHelper.updateUser(user)) {
            ImageViewCompat.setImageTintList(binding.ivProfileAvatar, null);
            binding.ivProfileAvatar.setPadding(0, 0, 0, 0);
            binding.ivProfileAvatar.setImageURI(Uri.fromFile(destFile));
            Toast.makeText(this, "تم تحديث الصورة ✓", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(Constants.KEY_IS_LOGGED_IN, false)
                .putBoolean(Constants.KEY_REMEMBER_ME, false)
                .putInt(Constants.KEY_USER_ID, -1)
                .apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}