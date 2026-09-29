package com.example.englishvoicecoach;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class BeginnerCourse {
    final String level;
    final List<SessionBlock> session;
    final List<Day> days;
    final List<VocabularyWord> dictionary;

    private BeginnerCourse(String level, List<SessionBlock> session, List<Day> days) {
        this.level = level;
        this.session = Collections.unmodifiableList(session);
        this.days = Collections.unmodifiableList(days);
        Map<String, VocabularyWord> words = new LinkedHashMap<>();
        for (Day day : days) {
            for (VocabularyWord word : day.words) {
                words.putIfAbsent(word.word.toLowerCase(java.util.Locale.ROOT), word);
            }
        }
        dictionary = Collections.unmodifiableList(new ArrayList<>(words.values()));
    }

    static BeginnerCourse load(Context context) throws IOException, JSONException {
        byte[] data;
        try (InputStream input = context.getAssets().open("basic_course.json");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            data = output.toByteArray();
        }
        JSONObject root = new JSONObject(new String(data, StandardCharsets.UTF_8));
        List<SessionBlock> session = new ArrayList<>();
        JSONArray sessionArray = root.getJSONArray("session");
        for (int i = 0; i < sessionArray.length(); i++) {
            JSONObject item = sessionArray.getJSONObject(i);
            session.add(new SessionBlock(item.getString("activity"), item.getInt("minutes")));
        }
        List<Day> days = new ArrayList<>();
        JSONArray dayArray = root.getJSONArray("days");
        for (int i = 0; i < dayArray.length(); i++) {
            JSONObject item = dayArray.getJSONObject(i);
            days.add(parseDay(item));
        }
        return new BeginnerCourse(root.getString("level"), session, days);
    }

    private static Day parseDay(JSONObject item) throws JSONException {
        List<VocabularyWord> words = new ArrayList<>();
        JSONArray wordArray = item.getJSONArray("words");
        for (int i = 0; i < wordArray.length(); i++) {
            JSONObject word = wordArray.getJSONObject(i);
            words.add(new VocabularyWord(
                    word.getString("word"), word.getString("bn"), word.getString("meaning")
            ));
        }
        List<PracticeSentence> sentences = new ArrayList<>();
        JSONArray sentenceArray = item.getJSONArray("sentences");
        for (int i = 0; i < sentenceArray.length(); i++) {
            JSONObject sentence = sentenceArray.getJSONObject(i);
            sentences.add(new PracticeSentence(sentence.getString("en"), sentence.getString("bn")));
        }
        JSONObject rolePlay = item.getJSONObject("roleplay");
        return new Day(
                item.getInt("day"), item.getString("title"), item.getString("focus"),
                rolePlay.getString("en"), rolePlay.getString("bn"), words, sentences
        );
    }

    static final class SessionBlock {
        final String activity;
        final int minutes;

        SessionBlock(String activity, int minutes) {
            this.activity = activity;
            this.minutes = minutes;
        }
    }

    static final class Day {
        final int number;
        final String title;
        final String focus;
        final String rolePlay;
        final String bengaliRolePlay;
        final List<VocabularyWord> words;
        final List<PracticeSentence> sentences;

        Day(int number, String title, String focus, String rolePlay, String bengaliRolePlay,
            List<VocabularyWord> words, List<PracticeSentence> sentences) {
            this.number = number;
            this.title = title;
            this.focus = focus;
            this.rolePlay = rolePlay;
            this.bengaliRolePlay = bengaliRolePlay;
            this.words = Collections.unmodifiableList(words);
            this.sentences = Collections.unmodifiableList(sentences);
        }
    }

    static final class VocabularyWord {
        final String word;
        final String bengali;
        final String definition;

        VocabularyWord(String word, String bengali, String definition) {
            this.word = word;
            this.bengali = bengali;
            this.definition = definition;
        }
    }

    static final class PracticeSentence {
        final String english;
        final String bengali;

        PracticeSentence(String english, String bengali) {
            this.english = english;
            this.bengali = bengali;
        }
    }
}
