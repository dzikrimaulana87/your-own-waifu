package com.example.waifuchatbot;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.waifuchatbot.model.ChatIntent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import jsastrawi.morphology.DefaultLemmatizer;
import jsastrawi.morphology.Lemmatizer;

import java.lang.reflect.Type;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private List<ChatIntent> intents;
    private TextView chatBox;
    private EditText userInput;

    private Lemmatizer lemmatizer;

    private final Set<String> stopwords = new HashSet<>(Arrays.asList(
            "yang", "untuk", "pada", "ke", "para", "namun", "menurut", "antara", "dia", "dua", "ia",
            "seperti", "jika", "sehingga", "kembali", "dan", "tidak", "ini", "karena", "kepada", "oleh",
            "saat", "harus", "sementara", "setelah", "belum", "kami", "sekitar", "bagi", "serta", "di",
            "dari", "telah", "sebagai", "masih", "hal", "ketika", "adalah", "itu", "dalam", "bisa",
            "bahwa", "atau", "hanya", "kita", "dengan", "akan", "juga", "ada", "mereka", "sudah", "saya"
    ));
    InputStream in = Lemmatizer.class.getResourceAsStream("/root-words.txt");
    BufferedReader br = new BufferedReader(new InputStreamReader(in));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatBox = findViewById(R.id.chatBox);
        userInput = findViewById(R.id.userInput);

        // Inisialisasi lemmatizer
        try {
            Set<String> dictionary = new HashSet<>();
            InputStream is = getAssets().open("root-words.txt");
            Scanner scanner = new Scanner(is);
            while (scanner.hasNext()) {
                dictionary.add(scanner.next());
            }
            lemmatizer = new DefaultLemmatizer(dictionary);
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadDataset();
        displayImage("positive");
    }

    public void onSendClicked(View view) {
        String message = userInput.getText().toString().trim();
        chatBox.append("Kamu: " + message + "\n");
        String response = getResponse(message);
        chatBox.append("Waifu: " + response + "\n\n");
        userInput.setText("");
    }

    private void loadDataset() {
        try {
            // Mencoba untuk membaca dataset yang sudah diunggah
            File datasetFile = new File(getFilesDir(), "dataset.json");
            if (datasetFile.exists()) {
                // Membaca file JSON yang sudah diunggah
                InputStream is = new FileInputStream(datasetFile);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                String json = new String(buffer, StandardCharsets.UTF_8);

                Gson gson = new Gson();
                Type listType = new TypeToken<List<ChatIntent>>() {}.getType();
                intents = gson.fromJson(json, listType);

                if (intents != null) {
                    System.out.println("Jumlah intent: " + intents.size());
                    for (ChatIntent i : intents) {
                        System.out.println("Intent: " + i.tag + ", Patterns: " + i.patterns.size());
                    }
                } else {
                    System.out.println("Dataset tidak berhasil di-parse (null)");
                    // Jika dataset tidak bisa diparse, gunakan file default
                    loadDefaultDataset();
                }
            } else {
                // Jika file tidak ditemukan, gunakan dataset default
                loadDefaultDataset();
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Jika terjadi kesalahan, fallback ke dataset default
            loadDefaultDataset();
        }
    }

    private void loadDefaultDataset() {
        try {
            // Membaca file JSON default dari assets
            InputStream is = getAssets().open("dataset.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            Type listType = new TypeToken<List<ChatIntent>>() {}.getType();
            intents = gson.fromJson(json, listType);

            if (intents != null) {
                System.out.println("Jumlah intent (default): " + intents.size());
                for (ChatIntent i : intents) {
                    System.out.println("Intent: " + i.tag + ", Patterns: " + i.patterns.size());
                }
            } else {
                System.out.println("Dataset default tidak berhasil di-parse (null)");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Gagal memuat dataset default.");
        }
    }


    private String getResponse(String message) {
        String cleanedMessage = preprocess(message);

        double maxSim = -1;
        ChatIntent bestIntent = null;

        for (ChatIntent intent : intents) {
            for (String pattern : intent.patterns) {
                String cleanedPattern = preprocess(pattern);
                double sim = cosineSimilarity(cleanedMessage, cleanedPattern);
                if (sim > maxSim) {
                    maxSim = sim;
                    bestIntent = intent;
                }
            }
        }

        if (bestIntent != null && maxSim > 0.1) {
            List<String> responses = bestIntent.responses;
            String currEmotion = bestIntent.emotion;
            String respond =  responses.get(new Random().nextInt(responses.size()));
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String nama = prefs.getString("username", "Suamiku....🥰");
            respond = respond.replace("[nama]",nama);
            displayImage(currEmotion);
            return respond;

        } else {
            return "Maaf, aku belum paham maksudmu 😔";
        }
    }

    private String preprocess(String text) {
        text = text.toLowerCase().replaceAll("[^a-zA-Z\\s]", "");
        String[] words = text.split("\\s+");
        List<String> result = new ArrayList<>();
        for (String word : words) {
            if (!stopwords.contains(word)) {
                String lemma = lemmatizer.lemmatize(word); // lakukan lemmatization
                result.add(lemma);
            }

        }
        return String.join(" ", result);
    }

    private double cosineSimilarity(String a, String b) {
        Set<String> allWords = new HashSet<>();
        List<String> aWords = Arrays.asList(a.split("\\s+"));
        List<String> bWords = Arrays.asList(b.split("\\s+"));
        allWords.addAll(aWords);
        allWords.addAll(bWords);

        int[] vecA = new int[allWords.size()];
        int[] vecB = new int[allWords.size()];

        int i = 0;
        for (String word : allWords) {
            vecA[i] = Collections.frequency(aWords, word);
            vecB[i] = Collections.frequency(bWords, word);
            i++;
        }

        int dot = 0, normA = 0, normB = 0;
        for (i = 0; i < vecA.length; i++) {
            dot += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }

        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public void onConfigClicked(View view) {
        Intent intent = new Intent(this, ConfigurationActivity.class);
        startActivity(intent);
    }

    private void displayImage(String emotion) {
        // Tentukan nama file berdasarkan parameter 'emotion'
        File imageFile = new File(getFilesDir(), emotion + ".jpg");

        // Periksa apakah file ada di direktori internal
        if (imageFile.exists()) {
            // Membaca gambar dari file
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

            // Menampilkan gambar di ImageView
            ImageView imageViewEmotion = findViewById(R.id.imageViewEmotion);
            imageViewEmotion.setImageBitmap(bitmap);
        } else {
            try {
                // Load gambar default dari assets
                InputStream inputStream = getAssets().open("default_waifu.jpg");
                Bitmap defaultBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                // Tampilkan gambar default ke ImageView
                ImageView imageViewEmotion = findViewById(R.id.imageViewEmotion);
                imageViewEmotion.setImageBitmap(defaultBitmap);

                // Tampilkan Toast
                String message = "Kamu belum upload gambar:\n" + emotion + "\nMenampilkan default waifu 💗";
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Gagal memuat gambar default", Toast.LENGTH_SHORT).show();
            }
        }


    }





}
