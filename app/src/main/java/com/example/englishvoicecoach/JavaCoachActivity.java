package com.example.englishvoicecoach;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class JavaCoachActivity extends Activity {
    private static final int REQUEST_SPEECH = 41;
    private static final int INK = Color.rgb(32, 49, 45);
    private static final int MUTED = Color.rgb(99, 117, 110);
    private static final int GREEN = Color.rgb(26, 105, 87);
    private static final int PALE_GREEN = Color.rgb(226, 241, 232);
    private static final int PAPER = Color.rgb(246, 248, 244);
    private static final int WHITE = Color.WHITE;
    private static final int CORAL = Color.rgb(226, 122, 91);

    private LearningProgress.State progress;
    private TextToSpeech tts;
    private boolean ttsReady;
    private boolean showBengaliHelp;
    private String recognizedText;
    private Integer recognizedScore;
    private int selectedTab;
    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progress = LearningProgress.load(this);
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                ttsReady = true;
            }
        });
        getWindow().setStatusBarColor(PAPER);
        getWindow().setNavigationBarColor(PAPER);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        render();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_SPEECH || resultCode != RESULT_OK || data == null) return;
        ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (matches == null || matches.isEmpty()) return;
        recognizedText = matches.get(0);
        int index = progress.currentLessonIndex();
        if (index < LearningProgress.LESSONS.size()) {
            LearningProgress.Lesson lesson = LearningProgress.LESSONS.get(index);
            recognizedScore = LearningProgress.wordMatchScore(lesson.sentence, recognizedText);
            progress = LearningProgress.recordPractice(this, recognizedScore);
            speakFeedback(lesson, recognizedScore);
        }
        render();
    }

    private void render() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PAPER);
        root.setFitsSystemWindows(true);

        LinearLayout header = column();
        header.setPadding(dp(24), dp(18), dp(24), dp(14));
        header.addView(text("English Voice Coach", 21, INK, true));
        addTop(header, text("বাংলা সহায়তা  ·  BENGALI", 12, GREEN, true), 3);
        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        content = column();
        content.setPadding(dp(20), dp(6), dp(20), dp(24));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        if (!progress.onboarded) renderOnboarding();
        else if (selectedTab == 1) renderPath();
        else renderToday();

        if (progress.onboarded) root.addView(navigation());
        setContentView(root);
    }

    private void renderOnboarding() {
        label(content, "GETTING STARTED");
        addTop(content, text("আপনার মাতৃভাষা কী?", 28, INK, true), 10);
        addTop(content, text("শেখার সময় প্রয়োজন হলে বাংলা সহায়তা পাবেন।", 16, MUTED, false), 8);
        LinearLayout language = card();
        language.setOrientation(LinearLayout.HORIZONTAL);
        language.addView(text("●", 20, GREEN, true), new LinearLayout.LayoutParams(dp(34), -2));
        LinearLayout words = column();
        words.addView(text("বাংলা", 18, INK, true));
        addTop(words, text("Bengali", 14, MUTED, false), 2);
        language.addView(words);
        addTop(content, language, 24);
        Button start = button("শুরু করুন", true);
        start.setOnClickListener(v -> {
            progress = LearningProgress.finishOnboarding(this);
            render();
        });
        addTop(content, start, 20);
    }

    private void renderToday() {
        label(content, "YOUR NEXT STEP");
        addTop(content, text("A little English,\nevery day.", 30, INK, true), 9);
        addTop(content, text("Your course picks up where you left off.", 15, MUTED, false), 7);

        LinearLayout metrics = new LinearLayout(this);
        metrics.setOrientation(LinearLayout.HORIZONTAL);
        addMetric(metrics, String.valueOf(progress.completedLessons), "LESSONS", GREEN);
        addMetric(metrics, String.valueOf(progress.studyDays), "STUDY DAYS", CORAL);
        addMetric(metrics, String.valueOf(progress.streak), "DAY STREAK", INK);
        addTop(content, metrics, 20);

        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(LearningProgress.LESSONS.size());
        bar.setProgress(progress.completedLessons);
        bar.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));
        addTop(content, bar, 18);
        addTop(content, text(progress.completedLessons + " of " + LearningProgress.LESSONS.size() + " lessons complete", 13, MUTED, false), 5);

        int index = progress.currentLessonIndex();
        if (index >= LearningProgress.LESSONS.size()) {
            LinearLayout done = card();
            label(done, "COURSE COMPLETE");
            addTop(done, text("দারুণ কাজ!", 26, GREEN, true), 8);
            addTop(done, text("You completed the full learning path.", 16, INK, false), 6);
            addTop(content, done, 22);
            return;
        }

        LearningProgress.Lesson lesson = LearningProgress.LESSONS.get(index);
        LinearLayout lessonCard = card();
        label(lessonCard, "LESSON " + (index + 1) + "  ·  " + lesson.level.toUpperCase(Locale.US));
        addTop(lessonCard, text(lesson.title, 22, INK, true), 7);
        addTop(lessonCard, text(lesson.focus, 14, MUTED, false), 3);
        addTop(lessonCard, text("“" + lesson.sentence + "”", 18, INK, true), 18);
        addTop(content, lessonCard, 18);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button listen = button("Listen", false);
        listen.setOnClickListener(v -> speak(lesson.sentence));
        Button practice = button("Practice", true);
        practice.setOnClickListener(v -> startSpeechRecognition());
        actions.addView(listen, new LinearLayout.LayoutParams(0, dp(52), 1f));
        LinearLayout.LayoutParams practiceParams = new LinearLayout.LayoutParams(0, dp(52), 1f);
        practiceParams.leftMargin = dp(10);
        actions.addView(practice, practiceParams);
        addTop(content, actions, 12);

        if (recognizedText != null && recognizedScore != null) {
            LinearLayout result = card();
            label(result, "YOUR PRACTICE");
            addTop(result, text("“" + recognizedText + "”", 15, INK, false), 8);
            addTop(result, text(recognizedScore + "% word match", 16,
                    recognizedScore >= LearningProgress.PASS_SCORE ? GREEN : CORAL, true), 8);
            addTop(content, result, 14);
        }

        Button help = button(showBengaliHelp ? "Hide Bengali help" : "বাংলা সাহায্য  ·  Bengali help", false);
        help.setOnClickListener(v -> {
            showBengaliHelp = !showBengaliHelp;
            render();
        });
        addTop(content, help, 12);
        if (showBengaliHelp) {
            LinearLayout translation = card();
            label(translation, "বাংলা অর্থ");
            addTop(translation, text(lesson.bengaliTranslation, 17, INK, true), 7);
            addTop(translation, text(lesson.bengaliTip, 14, MUTED, false), 7);
            addTop(content, translation, 8);
        }
        if (progress.canAdvance()) {
            Button next = button("Continue to next lesson", true);
            next.setOnClickListener(v -> {
                progress = LearningProgress.completeLesson(this);
                recognizedText = null;
                recognizedScore = null;
                showBengaliHelp = false;
                render();
            });
            addTop(content, next, 14);
        }
    }

    private void renderPath() {
        label(content, "YOUR COURSE");
        addTop(content, text("Learning path", 29, INK, true), 8);
        addTop(content, text("Move forward as your English gets stronger.", 15, MUTED, false), 5);
        List<String> levels = Arrays.asList("Basic", "Beginner", "Intermediate", "Advanced");
        for (String level : levels) {
            LinearLayout section = column();
            addTop(section, text(level, 20, INK, true), 18);
            for (int i = 0; i < LearningProgress.LESSONS.size(); i++) {
                LearningProgress.Lesson lesson = LearningProgress.LESSONS.get(i);
                if (!lesson.level.equals(level)) continue;
                LinearLayout row = new LinearLayout(this);
                row.setGravity(Gravity.CENTER_VERTICAL);
                String marker = i < progress.completedLessons ? "✓" : (i == progress.currentLessonIndex() ? "•" : "○");
                int color = i < progress.completedLessons ? GREEN : (i == progress.currentLessonIndex() ? CORAL : MUTED);
                row.addView(text(marker, 20, color, true), new LinearLayout.LayoutParams(dp(32), -2));
                LinearLayout words = column();
                words.addView(text(lesson.title, 15, i <= progress.currentLessonIndex() ? INK : MUTED,
                        i == progress.currentLessonIndex()));
                addTop(words, text("Lesson " + (i + 1), 12, MUTED, false), 2);
                row.addView(words, new LinearLayout.LayoutParams(0, -2, 1f));
                if (i == progress.currentLessonIndex()) row.addView(text("NOW", 11, GREEN, true));
                section.addView(row, new LinearLayout.LayoutParams(-1, dp(62)));
                View divider = new View(this);
                divider.setBackgroundColor(0xFFE4EAE4);
                section.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));
            }
            content.addView(section);
        }
    }

    private LinearLayout navigation() {
        LinearLayout nav = new LinearLayout(this);
        nav.setPadding(dp(20), dp(10), dp(20), dp(12));
        nav.setBackgroundColor(WHITE);
        Button today = button("Today", selectedTab == 0);
        today.setOnClickListener(v -> { selectedTab = 0; render(); });
        Button path = button("Learning path", selectedTab == 1);
        path.setOnClickListener(v -> { selectedTab = 1; render(); });
        nav.addView(today, new LinearLayout.LayoutParams(0, dp(48), 1f));
        LinearLayout.LayoutParams pathParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
        pathParams.leftMargin = dp(10);
        nav.addView(path, pathParams);
        return nav;
    }

    private void addMetric(LinearLayout row, String value, String caption, int color) {
        LinearLayout tile = column();
        tile.setGravity(Gravity.CENTER_VERTICAL);
        tile.setPadding(dp(11), dp(9), dp(6), dp(9));
        tile.setBackground(background(WHITE, 0xFFE3EAE4, 13));
        tile.addView(text(value, 22, color, true));
        addTop(tile, text(caption, 9, MUTED, true), 2);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(72), 1f);
        if (row.getChildCount() > 0) params.leftMargin = dp(8);
        row.addView(tile, params);
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the English sentence aloud");
        try {
            startActivityForResult(intent, REQUEST_SPEECH);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, "Speech recognition is not available on this device.", Toast.LENGTH_LONG).show();
        }
    }

    private void speak(String sentence) {
        if (!ttsReady) {
            Toast.makeText(this, "Speech is still loading. Try again in a moment.", Toast.LENGTH_SHORT).show();
            return;
        }
        tts.setLanguage(Locale.US);
        tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, "lesson_sentence");
    }

    private void speakFeedback(LearningProgress.Lesson lesson, int score) {
        if (!ttsReady) return;
        tts.setLanguage(Locale.US);
        String feedback = score >= LearningProgress.PASS_SCORE
                ? "Good work. You matched " + score + " percent. " + lesson.sentence
                : "You matched " + score + " percent. Listen again and try once more. " + lesson.sentence;
        tts.speak(feedback, TextToSpeech.QUEUE_FLUSH, null, "practice_feedback");
    }

    private LinearLayout card() {
        LinearLayout view = column();
        view.setPadding(dp(18), dp(17), dp(18), dp(17));
        view.setBackground(background(WHITE, 0xFFE3EAE4, 16));
        return view;
    }

    private LinearLayout column() {
        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);
        return view;
    }

    private void label(LinearLayout parent, String value) {
        parent.addView(text(value, 11, GREEN, true));
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private Button button(String value, boolean primary) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setAllCaps(false);
        button.setTextColor(primary ? WHITE : GREEN);
        button.setPadding(dp(12), dp(8), dp(12), dp(8));
        button.setBackground(background(primary ? GREEN : PALE_GREEN, Color.TRANSPARENT, 14));
        return button;
    }

    private GradientDrawable background(int color, int stroke, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        if (stroke != Color.TRANSPARENT) drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private void addTop(LinearLayout parent, View child, int margin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(margin);
        parent.addView(child, params);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
