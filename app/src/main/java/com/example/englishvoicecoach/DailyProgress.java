package com.example.englishvoicecoach;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

final class DailyProgress {
    private static final String PREFS = "learner_progress";
    private static final int PASS_SCORE = 70;

    private DailyProgress() {}

    static final class State {
        final boolean onboarded;
        final int completedDays;
        final int activeDayIndex;
        final int passedMask;
        final int attempts;
        final int totalScore;
        final int lastScore;
        final int lastScoreSentence;
        final int studyDays;
        final int streak;
        final String lastCompletedDate;

        State(boolean onboarded, int completedDays, int activeDayIndex, int passedMask,
              int attempts, int totalScore, int lastScore, int lastScoreSentence,
              int studyDays, int streak, String lastCompletedDate) {
            this.onboarded = onboarded;
            this.completedDays = completedDays;
            this.activeDayIndex = activeDayIndex;
            this.passedMask = passedMask;
            this.attempts = attempts;
            this.totalScore = totalScore;
            this.lastScore = lastScore;
            this.lastScoreSentence = lastScoreSentence;
            this.studyDays = studyDays;
            this.streak = streak;
            this.lastCompletedDate = lastCompletedDate;
        }

        int firstUnpassedSentence(int sentenceCount) {
            return DailyProgress.firstUnpassedSentence(passedMask, sentenceCount);
        }

        boolean isSentencePassed(int index) {
            return (passedMask & (1 << index)) != 0;
        }

        boolean isDayComplete(int sentenceCount) {
            return DailyProgress.isDayComplete(passedMask, sentenceCount);
        }

        boolean isCourseComplete(int dayCount) {
            return completedDays >= dayCount;
        }

        boolean waitingForTomorrow(int dayCount) {
            return !isCourseComplete(dayCount)
                    && completedDays > 0
                    && activeDayIndex < completedDays;
        }

        int averageScore() {
            return attempts == 0 ? 0 : totalScore / attempts;
        }
    }

    static State load(Context context, int dayCount) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int completedDays = Math.max(0, Math.min(dayCount, prefs.getInt("completed_days", 0)));
        String completedDate = prefs.getString("last_completed_day_date", "");
        boolean waiting = completedDays > 0 && completedDays < dayCount
                && dayKey(0).equals(completedDate);
        int activeDay = completedDays >= dayCount
                ? Math.max(0, dayCount - 1)
                : (waiting ? completedDays - 1 : completedDays);
        int passedMask = activeDay < 0 ? 0 : prefs.getInt(maskKey(activeDay), 0);
        return new State(
                prefs.getBoolean("onboarded", false), completedDays, activeDay, passedMask,
                prefs.getInt("attempts", 0), prefs.getInt("total_score", 0),
                prefs.getInt("last_score", 0), prefs.getInt("last_score_sentence", -1),
                prefs.getInt("study_days", 0), prefs.getInt("streak", 0), completedDate
        );
    }

    static State finishOnboarding(Context context, int dayCount) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean("onboarded", true).putString("native_language", "bn-IN").apply();
        return load(context, dayCount);
    }

    static State recordPractice(Context context, int score, int sentenceIndex,
                                int dayCount, int sentenceCount) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        State old = load(context, dayCount);
        if (old.isCourseComplete(dayCount) || old.waitingForTomorrow(dayCount)) return old;

        String today = dayKey(0);
        int[] dayStats = updatedStudyStats(old, prefs.getString("last_study_day", ""), today);
        int newMask = markSentencePassed(old.passedMask, sentenceIndex, score);
        boolean dayComplete = isDayComplete(newMask, sentenceCount);
        int completedDays = old.completedDays;
        String completedDate = old.lastCompletedDate;
        String dailyTotalKey = "daily_score_total_" + today;
        String dailyCountKey = "daily_score_count_" + today;
        if (dayComplete && old.activeDayIndex == old.completedDays) {
            completedDays++;
            completedDate = today;
        }

        prefs.edit()
                .putInt("attempts", old.attempts + 1)
                .putInt("total_score", old.totalScore + score)
                .putInt("last_score", score)
                .putInt("last_score_sentence", sentenceIndex)
                .putInt("completed_days", completedDays)
                .putInt(maskKey(old.activeDayIndex), newMask)
                .putString("last_completed_day_date", completedDate)
                .putInt("study_days", dayStats[0])
                .putInt("streak", dayStats[1])
                .putInt(dailyTotalKey, prefs.getInt(dailyTotalKey, 0) + score)
                .putInt(dailyCountKey, prefs.getInt(dailyCountKey, 0) + 1)
                .putBoolean("study_day_" + today, true)
                .putString("last_study_day", today)
                .apply();
        return load(context, dayCount);
    }

    static State recordRolePlay(Context context, int dayCount) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        State old = load(context, dayCount);
        String today = dayKey(0);
        int[] dayStats = updatedStudyStats(old, prefs.getString("last_study_day", ""), today);
        prefs.edit().putInt("study_days", dayStats[0]).putInt("streak", dayStats[1])
            .putBoolean("study_day_" + today, true)
                .putString("last_study_day", today).apply();
        return load(context, dayCount);
    }

    static int markSentencePassed(int mask, int sentenceIndex, int score) {
        if (score < PASS_SCORE || sentenceIndex < 0 || sentenceIndex >= 31) return mask;
        return mask | (1 << sentenceIndex);
    }

    static int firstUnpassedSentence(int mask, int sentenceCount) {
        for (int i = 0; i < sentenceCount; i++) {
            if ((mask & (1 << i)) == 0) return i;
        }
        return sentenceCount;
    }

    static boolean isDayComplete(int mask, int sentenceCount) {
        return sentenceCount > 0 && firstUnpassedSentence(mask, sentenceCount) == sentenceCount;
    }

    static void saveLearnerName(Context context, String name) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString("learner_name", name.trim()).apply();
    }

    static String learnerName(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString("learner_name", "");
    }

    static void saveConfidence(Context context, int dayNumber, int rating) {
        int boundedRating = Math.max(1, Math.min(5, rating));
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putInt("confidence_day_" + dayNumber, boundedRating).apply();
    }

    static int[] weeklyScores(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int[] scores = new int[7];
        for (int dayOffset = 6; dayOffset >= 0; dayOffset--) {
            String date = dayKey(-dayOffset);
            int count = prefs.getInt("daily_score_count_" + date, 0);
            scores[6 - dayOffset] = count == 0 ? 0
                    : prefs.getInt("daily_score_total_" + date, 0) / count;
        }
        return scores;
    }

    static int[] weeklyStudy(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int[] days = new int[7];
        for (int dayOffset = 6; dayOffset >= 0; dayOffset--) {
            days[6 - dayOffset] = prefs.getBoolean("study_day_" + dayKey(-dayOffset), false) ? 1 : 0;
        }
        return days;
    }

    private static int[] updatedStudyStats(State old, String lastStudyDate, String today) {
        int days = old.studyDays;
        int streak = old.streak;
        if (!today.equals(lastStudyDate)) {
            days++;
            streak = dayKey(-1).equals(lastStudyDate) ? streak + 1 : 1;
        }
        return new int[]{days, streak};
    }

    private static String maskKey(int dayIndex) {
        return "passed_sentences_day_" + dayIndex;
    }

    private static String dayKey(int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, offset);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
    }
}
