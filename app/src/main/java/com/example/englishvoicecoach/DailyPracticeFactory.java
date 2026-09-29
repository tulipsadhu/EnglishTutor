package com.example.englishvoicecoach;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class DailyPracticeFactory {
    static final int SENTENCE_COUNT = 30;
    static final int NEW_COUNT = CourseSyllabus.NEW_SENTENCES_PER_DAY;

    private DailyPracticeFactory() {}

    static final class Prompt {
        final BeginnerCourse.PracticeSentence sentence;
        final boolean newToday;

        Prompt(BeginnerCourse.PracticeSentence sentence, boolean newToday) {
            this.sentence = sentence;
            this.newToday = newToday;
        }
    }

    static List<Prompt> forDay(int dayNumber, BeginnerCourse course) {
        return forDay(dayNumber, course.days);
    }

    static List<Prompt> forDay(int dayNumber, List<BeginnerCourse.Day> courseDays) {
        if (dayNumber < 1 || dayNumber > CourseSyllabus.COURSE_DAYS) {
            throw new IllegalArgumentException("Day must be between 1 and 90");
        }
        List<BeginnerCourse.PracticeSentence> current = sentencesForDay(dayNumber, courseDays);
        List<Prompt> prompts = new ArrayList<>(SENTENCE_COUNT);
        Set<String> used = new HashSet<>();
        for (int i = 0; i < NEW_COUNT; i++) {
            BeginnerCourse.PracticeSentence sentence = current.get(i);
            prompts.add(new Prompt(sentence, true));
            used.add(sentence.english);
        }

        if (dayNumber == 1) {
            for (int i = NEW_COUNT; i < current.size() && prompts.size() < SENTENCE_COUNT; i++) {
                BeginnerCourse.PracticeSentence sentence = current.get(i);
                if (used.add(sentence.english)) prompts.add(new Prompt(sentence, false));
            }
        } else {
            int[] reviewGaps = {1, 3, 7, 14, 30, 60, 2, 5, 10, 21};
            for (int gap : reviewGaps) {
                int sourceDay = dayNumber - gap;
                if (sourceDay < 1) continue;
                List<BeginnerCourse.PracticeSentence> source = sentencesForDay(sourceDay, courseDays);
                int offset = Math.floorMod(dayNumber * 7 + gap * 3, source.size());
                int addedFromInterval = 0;
                for (int i = 0; i < source.size() && prompts.size() < SENTENCE_COUNT; i++) {
                    BeginnerCourse.PracticeSentence sentence = source.get((offset + i) % source.size());
                    if (used.add(sentence.english)) {
                        prompts.add(new Prompt(sentence, false));
                        addedFromInterval++;
                    }
                    if (addedFromInterval == 4) break;
                    if (prompts.size() == SENTENCE_COUNT) break;
                }
                if (prompts.size() == SENTENCE_COUNT) break;
            }
            for (int sourceDay = dayNumber - 1; sourceDay >= 1 && prompts.size() < SENTENCE_COUNT; sourceDay--) {
                for (BeginnerCourse.PracticeSentence sentence : sentencesForDay(sourceDay, courseDays)) {
                    if (used.add(sentence.english)) prompts.add(new Prompt(sentence, false));
                    if (prompts.size() == SENTENCE_COUNT) break;
                }
            }
        }

        for (int i = NEW_COUNT; prompts.size() < SENTENCE_COUNT && i < current.size(); i++) {
            BeginnerCourse.PracticeSentence sentence = current.get(i);
            if (used.add(sentence.english)) prompts.add(new Prompt(sentence, false));
        }
        return prompts;
    }

    static List<BeginnerCourse.PracticeSentence> newSentences(int dayNumber, BeginnerCourse course) {
        List<BeginnerCourse.PracticeSentence> source = sentencesForDay(dayNumber, course.days);
        return new ArrayList<>(source.subList(0, Math.min(NEW_COUNT, source.size())));
    }

    private static List<BeginnerCourse.PracticeSentence> sentencesForDay(int dayNumber, List<BeginnerCourse.Day> courseDays) {
        if (dayNumber > courseDays.size()) {
            throw new IllegalStateException("Course content is missing day " + dayNumber);
        }
        List<BeginnerCourse.PracticeSentence> sentences = courseDays.get(dayNumber - 1).sentences;
        if (sentences.size() < NEW_COUNT) {
            throw new IllegalStateException("Course day " + dayNumber + " needs at least " + NEW_COUNT + " new sentences");
        }
        return sentences;
    }
}
