package com.example.waifuchatbot;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.*;

public class ConfigurationActivity extends AppCompatActivity {

    private static final String FILENAME = "dataset.json";
    private static final String IMAGE_EXTENSION = ".jpg"; // Ekstensi file gambar
    private static final int REQUEST_PERMISSION_CODE = 2001;
    private String emotion;

    private EditText configName;

    // Register activity result launcher for file picker
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    try {
                        copySelectedJsonToInternalStorage(uri);
                        Toast.makeText(this, "Dataset berhasil diunggah.", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(this, "Gagal mengunggah dataset.", Toast.LENGTH_SHORT).show();
                        Log.e("UploadJson", "Error saat menyalin file JSON", e);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        configName = findViewById(R.id.configName);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        configName.setText(prefs.getString("username", ""));

        try {
            copyDatasetFromAssetsToInternalStorage();
        } catch (IOException e) {
            Log.e("ConfigurationActivity", "Gagal menyalin dataset.json dari assets", e);
        }
    }

    public void simpanConfig(View view) {
        String namaKamu = configName.getText().toString();
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        prefs.edit().putString("username", namaKamu).apply();
        Toast.makeText(this, "Nama disimpan: " + namaKamu, Toast.LENGTH_SHORT).show();
    }

    private void copyDatasetFromAssetsToInternalStorage() throws IOException {
        File datasetFile = new File(getFilesDir(), FILENAME);
        if (!datasetFile.exists()) {
            try (InputStream in = getAssets().open(FILENAME);
                 OutputStream out = new FileOutputStream(datasetFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
            Log.d("ConfigurationActivity", "File dataset.json disalin ke internal storage.");
        }
    }

    public void uploadJsonDataset(View view) {
        // Android 11+ perlu izin MANAGE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                return;
            }
        } else {
            // Untuk Android 10 ke bawah
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_CODE);
                return;
            }
        }

        // Buka file picker
        openFilePicker();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        filePickerLauncher.launch(Intent.createChooser(intent, "Pilih file dataset JSON"));
    }

    private void copySelectedJsonToInternalStorage(Uri uri) throws IOException {
        File outFile = new File(getFilesDir(), FILENAME); // Selalu disimpan sebagai "dataset.json"

        // Hapus file lama jika ada
        if (outFile.exists() && !outFile.delete()) {
            throw new IOException("Gagal menghapus file lama: " + outFile.getAbsolutePath());
        }

        // Salin file dari URI ke internal storage dengan nama "dataset.json"
        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }

        // Notifikasi lokasi penyimpanan
        Toast.makeText(this, "Dataset berhasil disimpan sebagai " + FILENAME, Toast.LENGTH_SHORT).show();
        // Restart MainActivity agar dataset baru dimuat
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Tutup ConfigurationActivity agar tidak numpuk
    }

    public void deleteDataset(View view) {
        File directory = getFilesDir();
        File[] files = directory.listFiles();

        if (files != null) {
            int count = 0;
            for (File file : files) {
                if (file.isFile()) {
                    if (file.delete()) {
                        count++;
                    }
                }
            }
            Toast.makeText(this, count + " file berhasil dihapus.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Tidak ada file untuk dihapus.", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Tutup ConfigurationActivity agar tidak numpuk
    }

    // Fungsi untuk upload gambar berdasarkan emosi
    public void uploadImagePositive(View view) {
        selectImageForEmotion("positive");
    }

    public void uploadImageLoving(View view) {
        selectImageForEmotion("loving");
    }

    public void uploadImageSupportive(View view) {
        selectImageForEmotion("supportive");
    }

    public void uploadImageFlirty(View view) {
        selectImageForEmotion("flirty");
    }

    public void uploadImageMelancholic(View view) {
        selectImageForEmotion("melancholic");
    }

    public void uploadImageNeutral(View view) {
        selectImageForEmotion("neutral");
    }

    // Fungsi umum untuk memilih gambar berdasarkan nama emosi
    private void selectImageForEmotion(String emotion) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        this.emotion = emotion;
        imagePickerLauncher.launch(Intent.createChooser(intent, "Pilih gambar untuk " + emotion));
    }

    // Activity result launcher untuk memilih gambar
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    String emotion = this.emotion;
                    try {
                        String fileName = emotion + IMAGE_EXTENSION;
                        saveImageToInternalStorage(uri, fileName);
                        Toast.makeText(this, "Gambar " + emotion + " berhasil diunggah.", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(this, "Gagal mengunggah gambar " + emotion, Toast.LENGTH_SHORT).show();
                        Log.e("UploadImage", "Error saat menyalin gambar", e);
                    }
                }
            });


    private void saveImageToInternalStorage(Uri uri, String fileName) throws IOException {
        File imageFile = new File(getFilesDir(), fileName);

        // Hapus file lama jika ada
        if (imageFile.exists() && !imageFile.delete()) {
            throw new IOException("Gagal menghapus file lama: " + imageFile.getAbsolutePath());
        }

        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(imageFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }
}
