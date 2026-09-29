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
import android.speech.tts.UtteranceProgressListener;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LearningActivity extends Activity {
    private static final int REQUEST_SENTENCE = 41;
    private static final int REQUEST_ROLEPLAY = 42;
    private static final int PASS_SCORE = 70;
    private static final int INK = Color.rgb(32, 49, 45);
    private static final int MUTED = Color.rgb(99, 117, 110);
    private static final int GREEN = Color.rgb(26, 105, 87);
    private static final int PALE_GREEN = Color.rgb(226, 241, 232);
    private static final int PAPER = Color.rgb(246, 248, 244);
    private static final int WHITE = Color.WHITE;
    private static final int CORAL = Color.rgb(226, 122, 91);

    private BeginnerCourse course;
    private DailyProgress.State progress;
    private DictionaryService dictionaryService;
    private TextToSpeech tts;
    private CoachAvatarView avatar;
    private LinearLayout content;
    private LinearLayout dictionaryResults;
    private EditText dictionaryInput;
    private EditText nameInput;
    private String recognizedText;
    private String dictionaryMessage;
    private Integer recognizedScore;
    private boolean showBengaliHelp;
    private boolean showAllPrompts;
    private int selectedTab;
    private int voiceMode = CoachAvatarView.IDLE;
    private boolean ttsReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            course = BeginnerCourse.load(this);
        } catch (IOException | JSONException exception) {
            Toast.makeText(this, "The course could not be loaded.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        progress = DailyProgress.load(this, CourseSyllabus.COURSE_DAYS);
        dictionaryService = new DictionaryService();
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                ttsReady = true;
            }
        });
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String id) {
                runOnUiThread(() -> setVoiceMode(CoachAvatarView.SPEAKING));
            }

            @Override
            public void onDone(String id) {
                runOnUiThread(() -> setVoiceMode(CoachAvatarView.IDLE));
            }

            @Override
            public void onError(String id) {
                runOnUiThread(() -> setVoiceMode(CoachAvatarView.IDLE));
            }
        });
        getWindow().setStatusBarColor(PAPER);
        getWindow().setNavigationBarColor(PAPER);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );
        render();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (dictionaryService != null) dictionaryService.close();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setVoiceMode(CoachAvatarView.IDLE);
        if ((requestCode != REQUEST_SENTENCE && requestCode != REQUEST_ROLEPLAY)
                || resultCode != RESULT_OK || data == null) return;
        ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (matches == null || matches.isEmpty()) return;
        recognizedText = matches.get(0);
        int dayNumber = Math.min(progress.activeDayIndex + 1, CourseSyllabus.COURSE_DAYS);
        CourseSyllabus.DayPlan plan = CourseSyllabus.forDay(dayNumber);
        if (requestCode == REQUEST_ROLEPLAY) {
            progress = DailyProgress.recordRolePlay(this, CourseSyllabus.COURSE_DAYS);
            speak("Good job speaking freely. Try to give one detail and ask a follow-up question.", "roleplay_feedback");
        } else {
            List<DailyPracticeFactory.Prompt> prompts = promptsForDay(dayNumber);
            int sentenceIndex = progress.firstUnpassedSentence(prompts.size());
            if (sentenceIndex < prompts.size()) {
                String expected = prompts.get(sentenceIndex).sentence.english;
                recognizedScore = SpeechScoring.wordMatchScore(expected, recognizedText);
                progress = DailyProgress.recordPractice(this, recognizedScore, sentenceIndex,
                        CourseSyllabus.COURSE_DAYS, prompts.size());
                speakPracticeFeedback(recognizedScore);
            }
        }
        render();
    }

    private List<DailyPracticeFactory.Prompt> promptsForDay(int dayNumber) {
        return DailyPracticeFactory.forDay(dayNumber, course);
    }

    private void render() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PAPER);
        LinearLayout header = column();
        header.setPadding(dp(20), dp(13), dp(20), dp(10));
        header.addView(text("English Voice Coach", 20, INK, true));
        addTop(header, text("বাংলা  ·  পশ্চিমবঙ্গ", 12, GREEN, true), 2);
        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        content = column();
        content.setPadding(dp(17), dp(3), dp(17), dp(20));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        if (!progress.onboarded) {
            renderOnboarding();
        } else if (selectedTab == 1) {
            renderSpeakingSkills();
        } else if (selectedTab == 2) {
            renderDictionary();
        } else if (selectedTab == 3) {
            renderCoursePath();
        } else if (selectedTab == 4) {
            renderProfile();
        } else {
            renderToday();
        }
        if (progress.onboarded) root.addView(navigation());
        setContentView(root);
    }

    private void renderOnboarding() {
        label(content, "GETTING STARTED");
        addTop(content, text("আপনার মাতৃভাষা কী?", 27, INK, true), 10);
        addTop(content, text("প্রয়োজন হলে পশ্চিমবঙ্গের বাংলায় সাহায্য পাবেন।", 16, MUTED, false), 7);
        LinearLayout language = card();
        language.addView(text("বাংলা", 19, INK, true));
        addTop(language, text("West Bengal Bengali", 14, GREEN, false), 3);
        addTop(content, language, 18);
        nameInput = new EditText(this);
        nameInput.setSingleLine(true);
        nameInput.setTextSize(16);
        nameInput.setHint("আপনার নাম  ·  Your name");
        nameInput.setText(DailyProgress.learnerName(this));
        nameInput.setPadding(dp(13), dp(8), dp(13), dp(8));
        nameInput.setBackground(background(WHITE, 0xFFE0E7E0, 12));
        addTop(content, nameInput, 14);
        Button start = button("শুরু করুন", true);
        start.setOnClickListener(view -> {
            DailyProgress.saveLearnerName(this, nameInput.getText().toString());
            progress = DailyProgress.finishOnboarding(this, CourseSyllabus.COURSE_DAYS);
            render();
        });
        addTop(content, start, 16);
    }

    private void renderToday() {
        int dayNumber = Math.min(progress.activeDayIndex + 1, CourseSyllabus.COURSE_DAYS);
        CourseSyllabus.DayPlan plan = CourseSyllabus.forDay(dayNumber);
        List<DailyPracticeFactory.Prompt> prompts = promptsForDay(dayNumber);
        int sentenceIndex = progress.firstUnpassedSentence(prompts.size());
        boolean complete = progress.isDayComplete(prompts.size());
        boolean waiting = progress.waitingForTomorrow(CourseSyllabus.COURSE_DAYS);

        LinearLayout coachRow = new LinearLayout(this);
        coachRow.setGravity(Gravity.CENTER_VERTICAL);
        avatar = new CoachAvatarView(this);
        avatar.setMode(voiceMode);
        coachRow.addView(avatar, new LinearLayout.LayoutParams(dp(78), dp(78)));
        LinearLayout coachInfo = column();
        coachInfo.addView(text("Mira", 18, INK, true));
        addTop(coachInfo, text(voiceStatus(), 13, MUTED, false), 3);
        LinearLayout.LayoutParams coachInfoParams = new LinearLayout.LayoutParams(0, -2, 1f);
        coachInfoParams.leftMargin = dp(12);
        coachRow.addView(coachInfo, coachInfoParams);
        content.addView(coachRow);

        label(content, plan.level.toUpperCase(Locale.ROOT) + "  ·  DAY " + dayNumber + " OF 90");
        addTop(content, text(plan.title, 26, INK, true), 6);
        addTop(content, text(plan.focus, 14, MUTED, false), 5);
        ProgressBar courseBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        courseBar.setMax(CourseSyllabus.COURSE_DAYS);
        courseBar.setProgress(progress.completedDays);
        courseBar.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));
        addTop(content, courseBar, 14);
        addTop(content, text(progress.completedDays + " of 90 days complete  ·  "
                + Integer.bitCount(progress.passedMask) + " of 30 prompts passed", 12, MUTED, false), 4);

        if (complete && waiting) {
            LinearLayout done = card();
            label(done, "TODAY COMPLETE");
            addTop(done, text("দারুণ কাজ!", 23, GREEN, true), 7);
            addTop(done, text("All 30 speaking turns are complete. The next day opens tomorrow.", 14, INK, false), 6);
            addTop(content, done, 13);
        } else if (sentenceIndex < prompts.size()) {
            renderCurrentPrompt(prompts.get(sentenceIndex), sentenceIndex);
            if (recognizedText != null && recognizedScore != null) renderResult();
            Button listButton = button(showAllPrompts ? "Hide today's prompts" : "View all 30 prompts", false);
            listButton.setOnClickListener(view -> {
                showAllPrompts = !showAllPrompts;
                render();
            });
            addTop(content, listButton, 12);
            if (showAllPrompts) renderPromptList(prompts, sentenceIndex);
        } else if (progress.isCourseComplete(CourseSyllabus.COURSE_DAYS)) {
            LinearLayout done = card();
            label(done, "COURSE COMPLETE");
            addTop(done, text("90 days of speaking practice. Well done!", 18, INK, true), 8);
            addTop(content, done, 14);
        }

        renderRolePlay(plan);
        Button path = button("Open the 90-day learning path", false);
        path.setOnClickListener(view -> { selectedTab = 3; render(); });
        addTop(content, path, 13);
    }

    private void renderCurrentPrompt(DailyPracticeFactory.Prompt prompt, int sentenceIndex) {
        LinearLayout sentenceCard = card();
        label(sentenceCard, prompt.newToday ? "NEW SENTENCE" : "SPACED REVIEW");
        addTop(sentenceCard, text("“" + prompt.sentence.english + "”", 19, INK, true), 8);
        Button translation = button(showBengaliHelp ? prompt.sentence.bengali : "বাংলা সাহায্য দেখুন", false);
        translation.setOnClickListener(view -> {
            showBengaliHelp = !showBengaliHelp;
            render();
        });
        addTop(sentenceCard, translation, 10);
        if (showBengaliHelp) {
            addTop(sentenceCard, text(prompt.sentence.bengali, 16, GREEN, true), 7);
            Button listenBengali = button("বাংলা শুনুন", false);
            listenBengali.setOnClickListener(view -> speakBengali(prompt.sentence.bengali));
            addTop(sentenceCard, listenBengali, 8);
        }
        addTop(content, sentenceCard, 14);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button listen = button("Listen", false);
        listen.setOnClickListener(view -> speak(prompt.sentence.english, "lesson_sentence"));
        Button practice = button("Speak", true);
        practice.setOnClickListener(view -> startSpeech(REQUEST_SENTENCE, "Say the English sentence aloud"));
        actions.addView(listen, new LinearLayout.LayoutParams(0, dp(49), 1f));
        LinearLayout.LayoutParams practiceParams = new LinearLayout.LayoutParams(0, dp(49), 1f);
        practiceParams.leftMargin = dp(8);
        actions.addView(practice, practiceParams);
        addTop(content, actions, 9);
    }

    private void renderResult() {
        LinearLayout result = card();
        label(result, "YOUR PRACTICE");
        addTop(result, text("“" + recognizedText + "”", 14, INK, false), 6);
        addTop(result, text(recognizedScore + "% word-order match", 15,
                recognizedScore >= PASS_SCORE ? GREEN : CORAL, true), 7);
        addTop(result, text("This checks recognized words, not pronunciation or accent.", 11, MUTED, false), 4);
        addTop(content, result, 10);
    }

    private void renderPromptList(List<DailyPracticeFactory.Prompt> prompts, int currentIndex) {
        for (int i = 0; i < prompts.size(); i++) {
            DailyPracticeFactory.Prompt prompt = prompts.get(i);
            String status = progress.isSentencePassed(i) ? "✓" : (i == currentIndex ? "NOW" : String.valueOf(i + 1));
            LinearLayout row = card();
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(text(status, 12, progress.isSentencePassed(i) ? GREEN : MUTED, true),
                    new LinearLayout.LayoutParams(dp(39), -2));
            LinearLayout sentenceText = column();
            sentenceText.addView(text(prompt.sentence.english, 14, INK, i == currentIndex));
            addTop(sentenceText, text(prompt.newToday ? "NEW" : "REVIEW", 10, GREEN, true), 3);
            row.addView(sentenceText, new LinearLayout.LayoutParams(0, -2, 1f));
            addTop(content, row, 6);
        }
    }

    private void renderRolePlay(CourseSyllabus.DayPlan plan) {
        LinearLayout roleplay = card();
        label(roleplay, "SPEAK FREELY");
        addTop(roleplay, text(plan.rolePlay, 15, INK, true), 7);
        addTop(roleplay, text(plan.bengaliRolePlay, 13, MUTED, false), 5);
        Button start = button("Start role-play", true);
        start.setOnClickListener(view -> startSpeech(REQUEST_ROLEPLAY, plan.rolePlay));
        addTop(roleplay, start, 10);
        if (recognizedText != null && selectedTab == 0) addTop(roleplay, text("You said: “" + recognizedText + "”", 13, GREEN, false), 8);
        addTop(content, roleplay, 16);
        LinearLayout sessionPlan = card();
        label(sessionPlan, "DAILY ROUTINE  ·  37 MINUTES");
        addTop(sessionPlan, text("5 min review · 12 min listening and speaking · 10 min conversation · 5 min reflection · 5 min recall", 13, MUTED, false), 7);
        addTop(content, sessionPlan, 12);
    }

    private void renderSpeakingSkills() {
        label(content, "SPEAKING SKILLS");
        addTop(content, text("Speak so people can follow you.", 25, INK, true), 8);
        addTop(content, text("Practise how to start, listen, respond and keep a conversation natural.", 14, MUTED, false), 5);
        String[][] skills = {
                {"Start simply", "Open with a greeting, then use the other person's name or ask an easy question.", "Hello, I’m Rina. How do you know the host?"},
                {"Listen, then respond", "Respond to one detail they said before changing the subject.", "You work near Park Street? How long have you been there?"},
                {"Ask a follow-up", "Use who, what, where, when or why to invite a fuller answer.", "What do you enjoy most about your work?"},
                {"Make your meaning clear", "Say the main point first. Add one reason or example.", "I prefer the Metro because it is quicker during rush hour."},
                {"Take your time", "Pause between ideas. A short pause sounds clearer than rushing.", "Let me think for a moment. I would choose the earlier train."},
                {"Repair a misunderstanding", "Ask for repetition or explain what you meant without apologising repeatedly.", "Could you say that another way? I meant the meeting time."},
                {"Disagree politely", "Acknowledge the other view, then share your own reason.", "I see your point. I think the other option may be easier."},
                {"Close warmly", "Summarise the plan and end with a friendly phrase.", "So, we’ll meet outside the station at six. See you then."}
        };
        for (String[] skill : skills) {
            LinearLayout card = card();
            card.addView(text(skill[0], 17, INK, true));
            addTop(card, text(skill[1], 13, MUTED, false), 5);
            addTop(card, text("“" + skill[2] + "”", 14, GREEN, false), 7);
            Button listen = button("Listen to example", false);
            listen.setOnClickListener(view -> speak(skill[2], "conversation_skill"));
            addTop(card, listen, 7);
            addTop(content, card, 8);
        }
        renderConfidenceCheck(content, progress.activeDayIndex + 1);
    }

    private void renderDictionary() {
        label(content, "WORD BANK");
        addTop(content, text("Dictionary", 27, INK, true), 7);
        addTop(content, text(course.dictionary.size() + " course words · Bengali meanings available offline", 13, MUTED, false), 4);
        addTop(content, text("Other English words are looked up online; only the searched word is sent to the dictionary service.", 12, MUTED, false), 7);
        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        dictionaryInput = new EditText(this);
        dictionaryInput.setSingleLine(true);
        dictionaryInput.setTextSize(15);
        dictionaryInput.setHint("Search one English word");
        dictionaryInput.setPadding(dp(11), dp(8), dp(11), dp(8));
        dictionaryInput.setBackground(background(WHITE, 0xFFE1E8E1, 11));
        searchRow.addView(dictionaryInput, new LinearLayout.LayoutParams(0, dp(49), 1f));
        Button search = button("Look up", true);
        search.setOnClickListener(view -> lookupWord(dictionaryInput.getText().toString()));
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(-2, dp(49));
        searchParams.leftMargin = dp(7);
        searchRow.addView(search, searchParams);
        addTop(content, searchRow, 13);
        dictionaryResults = column();
        addTop(content, dictionaryResults, 10);
        showLocalWords("");
    }

    private void lookupWord(String query) {
        String cleaned = query.trim().toLowerCase(Locale.ROOT);
        if (cleaned.isEmpty()) {
            showLocalWords("");
            return;
        }
        List<BeginnerCourse.VocabularyWord> matches = new ArrayList<>();
        for (BeginnerCourse.VocabularyWord word : course.dictionary) {
            if (word.word.toLowerCase(Locale.ROOT).contains(cleaned)) matches.add(word);
        }
        if (!matches.isEmpty()) {
            dictionaryResults.removeAllViews();
            for (BeginnerCourse.VocabularyWord word : matches) addWordCard(dictionaryResults, word);
            return;
        }
        dictionaryResults.removeAllViews();
        dictionaryResults.addView(text("Looking up “" + query + "”…", 14, MUTED, false));
        dictionaryService.lookup(query, new DictionaryService.Callback() {
            @Override
            public void onResult(DictionaryService.Entry entry) {
                if (isFinishing() || dictionaryResults == null) return;
                dictionaryResults.removeAllViews();
                LinearLayout card = card();
                card.addView(text(entry.word, 19, INK, true));
                if (!entry.phonetic.isEmpty()) addTop(card, text(entry.phonetic, 14, GREEN, false), 4);
                for (String definition : entry.definitions) addTop(card, text(definition, 14, INK, false), 8);
                String source = "Free Dictionary API · Wiktionary contributors · " + entry.license
                        + (entry.sources.isEmpty() ? "" : " · " + entry.sources.get(0));
                TextView attribution = text(source, 10, MUTED, false);
                attribution.setAutoLinkMask(Linkify.WEB_URLS);
                attribution.setMovementMethod(LinkMovementMethod.getInstance());
                addTop(card, attribution, 11);
                dictionaryResults.addView(card);
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || dictionaryResults == null) return;
                dictionaryResults.removeAllViews();
                dictionaryResults.addView(text(message, 14, CORAL, false));
            }
        });
    }

    private void showLocalWords(String query) {
        if (dictionaryResults == null) return;
        dictionaryResults.removeAllViews();
        String cleaned = query.trim().toLowerCase(Locale.ROOT);
        for (BeginnerCourse.VocabularyWord word : course.dictionary) {
            if (cleaned.isEmpty() || word.word.toLowerCase(Locale.ROOT).contains(cleaned)) {
                addWordCard(dictionaryResults, word);
            }
        }
    }

    private void addWordCard(LinearLayout parent, BeginnerCourse.VocabularyWord word) {
        LinearLayout card = card();
        card.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout meaning = column();
        meaning.addView(text(word.word, 15, INK, true));
        addTop(meaning, text(word.bengali + " · " + word.definition, 13, MUTED, false), 4);
        card.addView(meaning, new LinearLayout.LayoutParams(0, -2, 1f));
        Button listen = button("Listen", false);
        listen.setOnClickListener(view -> speak(word.word, "word_audio"));
        card.addView(listen, new LinearLayout.LayoutParams(-2, dp(43)));
        addTop(parent, card, 7);
    }

    private void renderCoursePath() {
        label(content, "90-DAY COURSE");
        addTop(content, text("Your learning path", 26, INK, true), 7);
        addTop(content, text("Three stages build from everyday phrases to confident conversations.", 14, MUTED, false), 5);
        addStageHeading(content, "BASIC", "Days 1–30");
        addPlanRows(content, 1, 30);
        addStageHeading(content, "EVERYDAY", "Days 31–60");
        addPlanRows(content, 31, 60);
        addStageHeading(content, "CONFIDENT CONVERSATION", "Days 61–90");
        addPlanRows(content, 61, 90);
    }

    private void addStageHeading(LinearLayout parent, String name, String days) {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(text(name, 16, GREEN, true), new LinearLayout.LayoutParams(0, -2, 1f));
        header.addView(text(days, 12, MUTED, false));
        addTop(parent, header, 17);
    }

    private void addPlanRows(LinearLayout parent, int firstDay, int lastDay) {
        for (int dayNumber = firstDay; dayNumber <= lastDay; dayNumber++) {
            CourseSyllabus.DayPlan plan = CourseSyllabus.forDay(dayNumber);
            boolean complete = dayNumber <= progress.completedDays;
            boolean active = dayNumber == progress.activeDayIndex + 1;
            LinearLayout row = card();
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(text(complete ? "✓" : active ? "•" : "○", 17,
                    complete ? GREEN : active ? CORAL : MUTED, true), new LinearLayout.LayoutParams(dp(30), -2));
            LinearLayout words = column();
            words.addView(text("Day " + dayNumber + " · " + plan.title, 14,
                    complete || active ? INK : MUTED, active));
            addTop(words, text(plan.focus, 11, MUTED, false), 3);
            row.addView(words, new LinearLayout.LayoutParams(0, -2, 1f));
            if (active) row.addView(text("NOW", 10, GREEN, true));
            addTop(parent, row, 6);
        }
    }

    private void renderProfile() {
        label(content, "YOUR PROFILE");
        String learnerName = DailyProgress.learnerName(this);
        addTop(content, text(learnerName.isEmpty() ? "English learner" : learnerName, 26, INK, true), 7);
        addTop(content, text("Native language · Bengali (West Bengal)", 13, MUTED, false), 4);
        EditText editName = new EditText(this);
        editName.setSingleLine(true);
        editName.setText(learnerName);
        editName.setHint("Your name");
        editName.setPadding(dp(11), dp(7), dp(11), dp(7));
        editName.setBackground(background(WHITE, 0xFFE1E8E1, 11));
        addTop(content, editName, 11);
        Button saveName = button("Save name", false);
        saveName.setOnClickListener(view -> {
            DailyProgress.saveLearnerName(this, editName.getText().toString());
            Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
        });
        addTop(content, saveName, 7);

        LinearLayout metrics = new LinearLayout(this);
        metrics.setOrientation(LinearLayout.HORIZONTAL);
        addMetric(metrics, progress.completedDays + "/90", "DAYS", GREEN);
        addMetric(metrics, String.valueOf(progress.studyDays), "STUDY DAYS", CORAL);
        addMetric(metrics, String.valueOf(progress.streak), "DAY STREAK", INK);
        addTop(content, metrics, 15);

        LinearLayout completion = card();
        label(completion, "COURSE COMPLETION");
        ProgressBar courseBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        courseBar.setMax(CourseSyllabus.COURSE_DAYS);
        courseBar.setProgress(progress.completedDays);
        courseBar.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));
        addTop(completion, courseBar, 9);
        addTop(completion, text(progress.completedDays + "% of the 90-day path", 13, MUTED, false), 5);
        addTop(content, completion, 11);

        LinearLayout speaking = card();
        label(speaking, "WEEKLY SPEAKING SCORE");
        speaking.addView(new ProgressChartView(this, DailyProgress.weeklyScores(this), true),
                new LinearLayout.LayoutParams(-1, dp(140)));
        addTop(content, speaking, 9);

        LinearLayout consistency = card();
        label(consistency, "STUDY CONSISTENCY · LAST 7 DAYS");
        consistency.addView(new ProgressChartView(this, DailyProgress.weeklyStudy(this), false),
                new LinearLayout.LayoutParams(-1, dp(110)));
        addTop(content, consistency, 9);
        renderConfidenceCheck(content, progress.activeDayIndex + 1);
    }

    private void renderConfidenceCheck(LinearLayout parent, int dayNumber) {
        LinearLayout reflection = card();
        label(reflection, "CONFIDENCE CHECK-IN");
        addTop(reflection, text("How confident did you feel speaking today?", 14, INK, false), 6);
        LinearLayout ratings = new LinearLayout(this);
        ratings.setOrientation(LinearLayout.HORIZONTAL);
        for (int rating = 1; rating <= 5; rating++) {
            int selected = rating;
            Button choice = button(String.valueOf(rating), false);
            choice.setOnClickListener(view -> {
                DailyProgress.saveConfidence(this, dayNumber, selected);
                Toast.makeText(this, "Reflection saved", Toast.LENGTH_SHORT).show();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(43), 1f);
            if (rating > 1) params.leftMargin = dp(5);
            ratings.addView(choice, params);
        }
        addTop(reflection, ratings, 7);
        addTop(parent, reflection, 11);
    }

    private LinearLayout navigation() {
        LinearLayout nav = new LinearLayout(this);
        nav.setPadding(dp(7), dp(8), dp(7), dp(10));
        nav.setBackgroundColor(WHITE);
        addNavigationItem(nav, "Today", 0);
        addNavigationItem(nav, "Talk", 1);
        addNavigationItem(nav, "Words", 2);
        addNavigationItem(nav, "Path", 3);
        addNavigationItem(nav, "Profile", 4);
        return nav;
    }

    private void addNavigationItem(LinearLayout parent, String title, int tab) {
        Button button = button(title, selectedTab == tab);
        button.setTextSize(11);
        button.setOnClickListener(view -> { selectedTab = tab; render(); });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(43), 1f);
        if (parent.getChildCount() > 0) params.leftMargin = dp(4);
        parent.addView(button, params);
    }

    private void addMetric(LinearLayout row, String value, String title, int accent) {
        LinearLayout tile = column();
        tile.setGravity(Gravity.CENTER_VERTICAL);
        tile.setPadding(dp(9), dp(8), dp(6), dp(8));
        tile.setBackground(background(WHITE, 0xFFE2E9E2, 12));
        tile.addView(text(value, 20, accent, true));
        addTop(tile, text(title, 9, MUTED, true), 2);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(67), 1f);
        if (row.getChildCount() > 0) params.leftMargin = dp(6);
        row.addView(tile, params);
    }

    private void startSpeech(int requestCode, String prompt) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
        try {
            setVoiceMode(CoachAvatarView.LISTENING);
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException exception) {
            setVoiceMode(CoachAvatarView.IDLE);
            Toast.makeText(this, "Speech recognition is not available on this device.", Toast.LENGTH_LONG).show();
        }
    }

    private void speak(String phrase, String utteranceId) {
        if (!ttsReady) {
            Toast.makeText(this, "Speech is still loading. Try again in a moment.", Toast.LENGTH_SHORT).show();
            return;
        }
        tts.setLanguage(Locale.US);
        setVoiceMode(CoachAvatarView.SPEAKING);
        tts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    private void speakBengali(String phrase) {
        if (!ttsReady) return;
        Locale bengaliIndia = new Locale("bn", "IN");
        if (tts.isLanguageAvailable(bengaliIndia) < TextToSpeech.LANG_AVAILABLE) {
            Toast.makeText(this, "A Bengali voice is not installed on this device.", Toast.LENGTH_LONG).show();
            return;
        }
        tts.setLanguage(bengaliIndia);
        setVoiceMode(CoachAvatarView.SPEAKING);
        tts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "bengali_help");
    }

    private void speakPracticeFeedback(int score) {
        String feedback = score >= PASS_SCORE
                ? "Good work. You matched " + score + " percent. Keep speaking clearly."
                : "You matched " + score + " percent. Listen once more, then try again.";
        speak(feedback, "practice_feedback");
    }

    private void setVoiceMode(int mode) {
        voiceMode = mode;
        if (avatar != null) avatar.setMode(mode);
    }

    private String voiceStatus() {
        if (voiceMode == CoachAvatarView.LISTENING) return "Listening";
        if (voiceMode == CoachAvatarView.SPEAKING) return "Speaking";
        return "Your speaking coach";
    }

    private LinearLayout card() {
        LinearLayout card = column();
        card.setPadding(dp(15), dp(14), dp(15), dp(14));
        card.setBackground(background(WHITE, 0xFFE2E9E2, 14));
        return card;
    }

    private LinearLayout column() {
        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);
        return view;
    }

    private void label(LinearLayout parent, String value) {
        parent.addView(text(value, 10, GREEN, true));
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
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(primary ? WHITE : GREEN);
        button.setPadding(dp(8), dp(5), dp(8), dp(5));
        button.setBackground(background(primary ? GREEN : PALE_GREEN, Color.TRANSPARENT, 12));
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
