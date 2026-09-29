package com.example.englishvoicecoach;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
        if (dayNumber < 1 || dayNumber > CourseSyllabus.COURSE_DAYS) {
            throw new IllegalArgumentException("Day must be between 1 and 90");
        }
        List<BeginnerCourse.PracticeSentence> current = sentencesForDay(dayNumber, course);
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
                List<BeginnerCourse.PracticeSentence> source = sentencesForDay(sourceDay, course);
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
                for (BeginnerCourse.PracticeSentence sentence : sentencesForDay(sourceDay, course)) {
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
        List<BeginnerCourse.PracticeSentence> source = sentencesForDay(dayNumber, course);
        return new ArrayList<>(source.subList(0, Math.min(NEW_COUNT, source.size())));
    }

    private static List<BeginnerCourse.PracticeSentence> sentencesForDay(int dayNumber, BeginnerCourse course) {
        if (dayNumber <= course.days.size()) {
            return course.days.get(dayNumber - 1).sentences;
        }
        CourseSyllabus.DayPlan plan = CourseSyllabus.forDay(dayNumber);
        return generatedSentences(plan);
    }

    static List<BeginnerCourse.PracticeSentence> generatedSentences(CourseSyllabus.DayPlan plan) {
        String englishTopic = plan.title.toLowerCase(Locale.ROOT);
        String bengaliTopic = plan.bengaliTopic;
        String[][] patterns = {
                {"I want to learn more about %s.", "আমি %s নিয়ে আরও জানতে চাই।"},
                {"Could you tell me something about %s?", "%s সম্পর্কে কিছু বলবেন?"},
                {"Let's talk about %s for a minute.", "চলো এক মিনিট %s নিয়ে কথা বলি।"},
                {"I need more practice talking about %s.", "%s নিয়ে কথা বলার আমার আরও অনুশীলন দরকার।"},
                {"What do you already know about %s?", "%s সম্পর্কে তুমি আগে থেকেই কী জানো?"},
                {"I can give one example about %s.", "%s নিয়ে আমি একটা উদাহরণ দিতে পারি।"},
                {"My friend asked me about %s.", "আমার বন্ধু আমাকে %s নিয়ে জিজ্ঞেস করেছিল।"},
                {"I would like to hear your opinion about %s.", "%s নিয়ে তোমার মতামত শুনতে চাই।"},
                {"Could you explain your experience with %s?", "%s নিয়ে তোমার অভিজ্ঞতার কথা বুঝিয়ে বলবে?"},
                {"I feel more confident speaking about %s now.", "এখন %s নিয়ে কথা বলতে আমার বেশি আত্মবিশ্বাস হয়।"},
                {"We discussed %s in class.", "আমরা ক্লাসে %s নিয়ে আলোচনা করেছি।"},
                {"I have a question about %s.", "%s নিয়ে আমার একটা প্রশ্ন আছে।"},
                {"How do you feel about %s?", "%s নিয়ে তোমার কী মনে হয়?"},
                {"I used to find %s difficult.", "আগে %s আমার কঠিন লাগত।"},
                {"Now I can speak about %s more clearly.", "এখন আমি %s নিয়ে আরও স্পষ্ট করে বলতে পারি।"},
                {"Please give me a moment to think about %s.", "%s নিয়ে ভাবার জন্য আমাকে একটু সময় দাও।"},
                {"I would like to ask a follow-up question about %s.", "%s নিয়ে আমি আরও একটা প্রশ্ন করতে চাই।"},
                {"Let's compare our ideas about %s.", "চলো %s নিয়ে আমাদের ভাবনার তুলনা করি।"},
                {"I learned a new phrase related to %s.", "%s নিয়ে আমি একটা নতুন কথা শিখেছি।"},
                {"Can you share a short story about %s?", "%s নিয়ে একটা ছোট গল্প বলবে?"},
                {"I can describe %s in a few sentences.", "কয়েকটি বাক্যে আমি %s-এর বর্ণনা দিতে পারি।"},
                {"What is the most useful thing about %s?", "%s-এর সবচেয়ে কাজে লাগে এমন দিকটা কী?"},
                {"I agree with you about %s because it is practical.", "এটা বাস্তবসম্মত বলে %s নিয়ে আমি তোমার সঙ্গে একমত।"},
                {"I see it differently, but I understand your point about %s.", "আমি বিষয়টা অন্যভাবে দেখি, তবে %s নিয়ে তোমার কথাটা বুঝতে পারছি।"},
                {"Could you summarise what we said about %s?", "%s নিয়ে আমরা যা বললাম, সেটা সংক্ষেপে বলবে?"},
                {"I am going to practise %s again tomorrow.", "আমি কাল আবার %s নিয়ে অনুশীলন করব।"},
                {"One challenge with %s is finding the right words.", "%s নিয়ে একটা অসুবিধা হল ঠিক শব্দ খুঁজে পাওয়া।"},
                {"If I do not understand %s, I can ask again.", "আমি %s না বুঝলে আবার জিজ্ঞেস করতে পারি।"},
                {"Let's finish with one clear point about %s.", "চলো %s নিয়ে একটা স্পষ্ট কথা বলে শেষ করি।"},
                {"I am proud that I can now discuss %s in English.", "এখন ইংরেজিতে %s নিয়ে কথা বলতে পারি বলে আমার ভালো লাগছে।"}
        };
        List<BeginnerCourse.PracticeSentence> result = new ArrayList<>(patterns.length);
        for (String[] pattern : patterns) {
            result.add(new BeginnerCourse.PracticeSentence(
                    String.format(Locale.ROOT, pattern[0], englishTopic),
                    String.format(Locale.ROOT, pattern[1], bengaliTopic)
            ));
        }
        return result;
    }
}
