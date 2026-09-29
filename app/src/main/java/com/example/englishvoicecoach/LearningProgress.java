package com.example.englishvoicecoach;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class LearningProgress {
    static final int PASS_SCORE = 70;
    static final List<Lesson> LESSONS = createLessons();
    private static final String PREFS = "learner_progress";

    private LearningProgress() {}

    static final class Lesson {
        final String level;
        final String title;
        final String focus;
        final String sentence;
        final String bengaliTranslation;
        final String bengaliTip;

        Lesson(String level, String title, String focus, String sentence,
               String bengaliTranslation, String bengaliTip) {
            this.level = level;
            this.title = title;
            this.focus = focus;
            this.sentence = sentence;
            this.bengaliTranslation = bengaliTranslation;
            this.bengaliTip = bengaliTip;
        }
    }

    static final class State {
        final boolean onboarded;
        final int completedLessons;
        final int attempts;
        final int totalScore;
        final int lastScore;
        final int lastScoreLesson;
        final int studyDays;
        final int streak;

        State(boolean onboarded, int completedLessons, int attempts, int totalScore,
              int lastScore, int lastScoreLesson, int studyDays, int streak) {
            this.onboarded = onboarded;
            this.completedLessons = completedLessons;
            this.attempts = attempts;
            this.totalScore = totalScore;
            this.lastScore = lastScore;
            this.lastScoreLesson = lastScoreLesson;
            this.studyDays = studyDays;
            this.streak = streak;
        }

        int currentLessonIndex() {
            return Math.min(completedLessons, LESSONS.size());
        }

        int averageScore() {
            return attempts == 0 ? 0 : totalScore / attempts;
        }

        boolean canAdvance() {
            return lastScoreLesson == currentLessonIndex() && lastScore >= PASS_SCORE;
        }
    }

    static State load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return new State(
                prefs.getBoolean("onboarded", false),
                prefs.getInt("completed_lessons", 0),
                prefs.getInt("attempts", 0),
                prefs.getInt("total_score", 0),
                prefs.getInt("last_score", 0),
                prefs.getInt("last_score_lesson", -1),
                prefs.getInt("study_days", 0),
                prefs.getInt("streak", 0)
        );
    }

    static State finishOnboarding(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("onboarded", true)
                .putString("native_language", "bn")
                .apply();
        return load(context);
    }

    static State recordPractice(Context context, int score) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        State old = load(context);
        String today = dayKey(0);
        String lastDay = prefs.getString("last_study_day", "");
        int days = old.studyDays;
        int streak = old.streak;
        if (!today.equals(lastDay)) {
            days++;
            streak = dayKey(-1).equals(lastDay) ? streak + 1 : 1;
        }
        int lessonIndex = old.currentLessonIndex();
        prefs.edit()
                .putInt("attempts", old.attempts + 1)
                .putInt("total_score", old.totalScore + score)
                .putInt("last_score", score)
                .putInt("last_score_lesson", lessonIndex)
                .putInt("study_days", days)
                .putInt("streak", streak)
                .putString("last_study_day", today)
                .apply();
        return load(context);
    }

    static State completeLesson(Context context) {
        State state = load(context);
        if (state.canAdvance() && state.completedLessons < LESSONS.size()) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit()
                    .putInt("completed_lessons", state.completedLessons + 1)
                    .apply();
        }
        return load(context);
    }

    static int wordMatchScore(String expected, String actual) {
        List<String> expectedWords = words(expected);
        List<String> actualWords = words(actual);
        if (expectedWords.isEmpty() || actualWords.isEmpty()) return 0;

        int[] previous = new int[actualWords.size() + 1];
        for (String expectedWord : expectedWords) {
            int[] current = new int[actualWords.size() + 1];
            for (int j = 1; j <= actualWords.size(); j++) {
                if (expectedWord.equals(actualWords.get(j - 1))) {
                    current[j] = previous[j - 1] + 1;
                } else {
                    current[j] = Math.max(previous[j], current[j - 1]);
                }
            }
            previous = current;
        }
        return previous[actualWords.size()] * 100 / expectedWords.size();
    }

    private static List<String> words(String text) {
        String normalized = text.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}']+", " ").trim();
        if (normalized.isEmpty()) return Collections.emptyList();
        String[] split = normalized.split("\\s+");
        List<String> result = new ArrayList<>();
        Collections.addAll(result, split);
        return result;
    }

    private static String dayKey(int dayOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, dayOffset);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
    }

    private static List<Lesson> createLessons() {
        List<Lesson> lessons = new ArrayList<>();
        lessons.add(new Lesson("Basic", "Introduce yourself", "Say your name", "Hello, my name is Rina.", "হ্যালো, আমার নাম রিনা।", "নিজের নাম বলার সময় 'My name is...' দিয়ে শুরু করুন।"));
        lessons.add(new Lesson("Basic", "Where you live", "Talk about your home", "I live in Dhaka.", "আমি ঢাকায় থাকি।", "শহরের নামের আগে 'in' ব্যবহার করুন।"));
        lessons.add(new Lesson("Basic", "Your daily goal", "Describe a habit", "I am learning English every day.", "আমি প্রতিদিন ইংরেজি শিখছি।", "প্রতিদিনের অভ্যাস বোঝাতে 'every day' বলুন।"));
        lessons.add(new Lesson("Beginner", "At the cafe", "Make a polite request", "I would like a cup of tea, please.", "আমি এক কাপ চা চাই, দয়া করে।", "ভদ্রভাবে কিছু চাইতে 'I would like...' ব্যবহার করুন।"));
        lessons.add(new Lesson("Beginner", "Find the station", "Ask for directions", "Could you tell me where the station is?", "স্টেশনটি কোথায়, আপনি কি আমাকে বলতে পারেন?", "ভদ্র প্রশ্নে 'Could you tell me...' দিয়ে শুরু করা যায়।"));
        lessons.add(new Lesson("Beginner", "Plan your day", "Ask about a schedule", "What time does the bus leave?", "বাসটি কয়টায় ছাড়ে?", "সময় জানতে 'What time...' দিয়ে প্রশ্ন করুন।"));
        lessons.add(new Lesson("Intermediate", "Choose your route", "Explain a reason", "I usually take the bus because it is cheaper.", "আমি সাধারণত বাসে যাই, কারণ এটি সস্তা।", "কারণ যোগ করতে 'because' ব্যবহার করুন।"));
        lessons.add(new Lesson("Intermediate", "A good experience", "Connect contrasting ideas", "Although the weather was poor, we enjoyed the trip.", "আবহাওয়া খারাপ হলেও আমরা ভ্রমণটি উপভোগ করেছি।", "বিপরীত ধারণা জুড়তে 'although' ব্যবহার করুন।"));
        lessons.add(new Lesson("Intermediate", "Clarify an idea", "Ask for an explanation", "Could you explain what you mean by this phrase?", "এই কথাটির মাধ্যমে আপনি কী বোঝাতে চান, ব্যাখ্যা করবেন?", "কোনো ধারণা পরিষ্কার করতে 'Could you explain...' বলুন।"));
        lessons.add(new Lesson("Advanced", "Study effectively", "Give a detailed reason", "One reason I prefer studying in the morning is that I can concentrate more effectively.", "সকালে পড়তে পছন্দ করার একটি কারণ হলো, তখন আমি আরও মনোযোগ দিতে পারি।", "দীর্ঘ বাক্যে মূল বক্তব্যের পরে কারণটি ব্যাখ্যা করুন।"));
        lessons.add(new Lesson("Advanced", "A change of plan", "Use a conditional sentence", "Had I known about the schedule change, I would have left earlier.", "সময়সূচি বদলের কথা জানলে আমি আরও আগে বের হতাম।", "অতীতের কাল্পনিক পরিস্থিতিতে 'Had I known...' বলা যায়।"));
        lessons.add(new Lesson("Advanced", "Balance a discussion", "Present two sides", "While remote work offers flexibility, it can also make collaboration more challenging.", "দূর থেকে কাজ নমনীয়তা দিলেও, একসঙ্গে কাজ করা কঠিন হতে পারে।", "দুই দিক তুলনা করতে 'While...' দিয়ে শুরু করুন।"));
        return Collections.unmodifiableList(lessons);
    }
}