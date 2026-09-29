package com.example.englishvoicecoach;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class DictionaryService {
    interface Callback {
        void onResult(Entry entry);
        void onError(String message);
    }

    static final class Entry {
        final String word;
        final String phonetic;
        final String license;
        final List<String> definitions;
        final List<String> sources;

        Entry(String word, String phonetic, String license, List<String> definitions, List<String> sources) {
            this.word = word;
            this.phonetic = phonetic;
            this.license = license;
            this.definitions = definitions;
            this.sources = sources;
        }
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    void lookup(String input, Callback callback) {
        String word = input.trim();
        if (!word.matches("[A-Za-z][A-Za-z'-]{0,48}")) {
            callback.onError("Enter one English word using letters only.");
            return;
        }
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String encoded = URLEncoder.encode(word, StandardCharsets.UTF_8.name());
                URL url = new URL("https://api.dictionaryapi.dev/api/v2/entries/en/" + encoded);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(7000);
                connection.setReadTimeout(7000);
                connection.setRequestProperty("Accept", "application/json");
                int responseCode = connection.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    postError(callback, "No online entry found. The course word bank is still available.");
                    return;
                }
                String response = readAll(connection.getInputStream());
                Entry result = parse(response);
                mainHandler.post(() -> callback.onResult(result));
            } catch (Exception exception) {
                postError(callback, "Dictionary lookup is unavailable. Check your connection and try again.");
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    void close() {
        executor.shutdownNow();
    }

    private void postError(Callback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }

    private static String readAll(InputStream input) throws Exception {
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = stream.read(buffer)) != -1) output.write(buffer, 0, count);
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static Entry parse(String response) throws Exception {
        JSONArray entries = new JSONArray(response);
        if (entries.length() == 0) throw new IllegalArgumentException("Empty dictionary response");
        JSONObject root = entries.getJSONObject(0);
        String phonetic = root.optString("phonetic", "");
        JSONArray phonetics = root.optJSONArray("phonetics");
        if (phonetic.isEmpty() && phonetics != null) {
            for (int i = 0; i < phonetics.length(); i++) {
                String candidate = phonetics.getJSONObject(i).optString("text", "");
                if (!candidate.isEmpty()) {
                    phonetic = candidate;
                    break;
                }
            }
        }

        List<String> definitions = new ArrayList<>();
        JSONArray meanings = root.optJSONArray("meanings");
        if (meanings != null) {
            for (int i = 0; i < meanings.length() && definitions.size() < 4; i++) {
                JSONObject meaning = meanings.getJSONObject(i);
                JSONArray items = meaning.optJSONArray("definitions");
                if (items == null) continue;
                for (int j = 0; j < items.length() && definitions.size() < 4; j++) {
                    String definition = items.getJSONObject(j).optString("definition", "");
                    if (!definition.isEmpty()) {
                        definitions.add(meaning.optString("partOfSpeech", "") + ": " + definition);
                    }
                }
            }
        }
        List<String> sources = new ArrayList<>();
        JSONArray sourceUrls = root.optJSONArray("sourceUrls");
        if (sourceUrls != null) {
            for (int i = 0; i < sourceUrls.length(); i++) sources.add(sourceUrls.getString(i));
        }
        JSONObject licenseObject = root.optJSONObject("license");
        String license = licenseObject == null ? "license listed by the source" : licenseObject.optString("name", "license listed by the source");
        return new Entry(root.optString("word", ""), phonetic, license, definitions, sources);
    }
}
