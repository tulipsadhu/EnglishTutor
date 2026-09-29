package com.example.englishvoicecoach;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class CourseSyllabus {
    static final int COURSE_DAYS = 90;
    static final int NEW_SENTENCES_PER_DAY = 10;
    static final int DAILY_REVIEW_PROMPTS = 20;
    static final int DAILY_SPEAKING_TURNS = NEW_SENTENCES_PER_DAY + DAILY_REVIEW_PROMPTS;
    static final List<String> BASIC_TOPICS = Collections.unmodifiableList(Arrays.asList(
            "Greetings and names", "Personal details", "Family and friends", "Everyday routines",
            "Time and plans for the day", "Food and polite requests", "Home and neighbourhood",
            "Directions and local places", "Transport around Kolkata", "Shopping and prices",
            "Work and study", "Health and asking for help", "Talking about yesterday",
            "Plans and everyday conversation", "Clothes and getting ready", "Weather and seasons",
            "Hobbies and free time", "Describing people", "Things in your home", "Phone conversations",
            "At the post office", "At the bank and ATM", "Making an appointment", "At the library",
            "School and learning", "Pets and animals", "Celebrations and invitations", "Asking for repetition",
            "Review: a day in Kolkata", "Basic speaking check-in"
    ));
    static final List<String> EVERYDAY_TOPICS = Collections.unmodifiableList(Arrays.asList(
            "Weekend plans", "Travel and checking in", "A visit to the doctor", "Explaining a problem",
            "Describing a journey", "Comparing two places", "Sharing a past experience", "Making suggestions",
            "Accepting and declining invitations", "Giving clear directions", "Talking about work tasks",
            "Explaining a process", "Returning an item", "Making a complaint politely", "Asking follow-up questions",
            "Talking about films and books", "Expressing preferences", "Making arrangements", "Describing a memorable day",
            "Talking about goals", "Giving simple advice", "Agreeing and disagreeing politely", "Solving a misunderstanding",
            "Joining a group conversation", "Talking about local events", "Explaining a choice", "Telling a short story",
            "Preparing for an interview", "Review: everyday conversations", "Everyday speaking check-in"
    ));
    static final List<String> CONFIDENT_TOPICS = Collections.unmodifiableList(Arrays.asList(
            "Starting a conversation confidently", "Keeping small talk going", "Giving a clear opinion", "Supporting an opinion with reasons",
            "Disagreeing with respect", "Clarifying a misunderstanding", "Telling a story with a clear sequence", "Summarising a discussion",
            "Speaking in a meeting", "Making a short presentation", "Answering unexpected questions", "Talking about strengths",
            "Discussing a difficult choice", "Negotiating a plan", "Giving constructive feedback", "Responding to criticism calmly",
            "Talking about change", "Explaining a complicated process simply", "Describing a challenge you overcame", "Making a persuasive request",
            "Discussing community issues", "Comparing different viewpoints", "Speaking in a job interview", "Leading a group discussion",
            "Using tone and pauses effectively", "Repairing a conversation breakdown", "Telling a personal story", "Impromptu speaking practice",
            "Review: confident conversations", "90-day speaking reflection"
    ));
    private static final List<String> BASIC_TOPIC_BENGALI = Collections.unmodifiableList(Arrays.asList(
            "শুভেচ্ছা ও নাম", "ব্যক্তিগত পরিচয়", "পরিবার ও বন্ধুরা", "রোজকার কাজ", "সময় ও দিনের পরিকল্পনা",
            "খাবারের অর্ডার", "বাড়ি ও পাড়া", "আশেপাশের রাস্তা", "কলকাতার যাতায়াত", "কেনাকাটা ও দাম",
            "কাজ ও পড়াশোনা", "শরীর-স্বাস্থ্য ও সাহায্য", "গতকালের কথা", "রোজকার কথোপকথন",
            "জামাকাপড় ও তৈরি হওয়া", "আবহাওয়া ও ঋতু", "শখ ও অবসর", "মানুষের বর্ণনা", "বাড়ির জিনিসপত্র",
            "ফোনে কথা", "ডাকঘরের কাজ", "ব্যাংক ও এটিএম", "দেখা করার সময় ঠিক করা", "লাইব্রেরি",
            "স্কুল ও পড়াশোনা", "পোষ্য ও পশুপাখি", "উৎসব ও নিমন্ত্রণ", "কথা আবার বলতে বলা",
            "কলকাতার একটি দিন", "নিজের শেখার অগ্রগতি"
    ));
    private static final List<String> EVERYDAY_TOPIC_BENGALI = Collections.unmodifiableList(Arrays.asList(
            "সপ্তাহান্তের পরিকল্পনা", "ভ্রমণ ও হোটেলে ওঠা", "ডাক্তারের কাছে যাওয়া", "সমস্যার কথা বোঝানো",
            "যাত্রার বর্ণনা", "দুটো জায়গার তুলনা", "আগের অভিজ্ঞতা ভাগ করে নেওয়া", "পরামর্শ দেওয়া",
            "নিমন্ত্রণ গ্রহণ বা না করা", "স্পষ্ট করে রাস্তা বোঝানো", "কাজের দায়িত্ব", "কোনও কাজের পদ্ধতি বোঝানো",
            "জিনিস ফেরত দেওয়া", "ভদ্রভাবে অভিযোগ করা", "পরের প্রশ্ন করা", "সিনেমা ও বই নিয়ে কথা",
            "পছন্দ-অপছন্দ বলা", "দেখা করার ব্যবস্থা করা", "মনে থাকার মতো একটি দিন", "নিজের লক্ষ্য নিয়ে কথা",
            "সহজ পরামর্শ দেওয়া", "ভদ্রভাবে একমত বা দ্বিমত হওয়া", "ভুল বোঝাবুঝি মেটানো", "দলের কথায় যোগ দেওয়া",
            "এলাকার অনুষ্ঠান", "কোনও সিদ্ধান্তের কারণ বলা", "ছোট গল্প বলা", "চাকরির সাক্ষাৎকারের প্রস্তুতি",
            "রোজকার কথোপকথনের পুনরালোচনা", "নিজের কথার দক্ষতার অগ্রগতি"
    ));
    private static final List<String> CONFIDENT_TOPIC_BENGALI = Collections.unmodifiableList(Arrays.asList(
            "আত্মবিশ্বাসের সঙ্গে আলাপ শুরু", "ছোট কথাবার্তা চালিয়ে যাওয়া", "নিজের মত স্পষ্ট করে বলা", "কারণ দিয়ে মত বোঝানো",
            "সম্মান রেখে দ্বিমত হওয়া", "ভুল বোঝাবুঝি পরিষ্কার করা", "ঘটনার ক্রম মেনে গল্প বলা", "আলোচনার সারাংশ বলা",
            "মিটিংয়ে কথা বলা", "ছোট বক্তৃতা দেওয়া", "হঠাৎ করা প্রশ্নের উত্তর", "নিজের শক্তির দিক বলা",
            "কঠিন সিদ্ধান্ত নিয়ে আলোচনা", "পরিকল্পনা নিয়ে দরদাম", "গঠনমূলক মতামত দেওয়া", "সমালোচনার শান্ত উত্তর",
            "পরিবর্তন নিয়ে কথা", "কঠিন বিষয় সহজ করে বোঝানো", "কোনও সমস্যার মোকাবিলা", "যুক্তি দিয়ে অনুরোধ করা",
            "এলাকার সমস্যা নিয়ে আলোচনা", "বিভিন্ন মতের তুলনা", "চাকরির সাক্ষাৎকারে কথা", "দলের আলোচনা পরিচালনা",
            "সুর ও বিরতি দিয়ে কথা বলা", "কথোপকথনের সমস্যা সামলানো", "ব্যক্তিগত অভিজ্ঞতার গল্প", "তাৎক্ষণিক বক্তৃতার অনুশীলন",
            "আত্মবিশ্বাসী কথোপকথনের পুনরালোচনা", "নব্বই দিনের শেখার প্রতিফলন"
    ));

    private CourseSyllabus() {}

    static DayPlan forDay(int dayNumber) {
        if (dayNumber < 1 || dayNumber > COURSE_DAYS) throw new IllegalArgumentException("Day must be between 1 and 90");
        int stageIndex = (dayNumber - 1) / 30;
        List<String> topics = stageIndex == 0 ? BASIC_TOPICS : stageIndex == 1 ? EVERYDAY_TOPICS : CONFIDENT_TOPICS;
        List<String> bengaliTopics = stageIndex == 0 ? BASIC_TOPIC_BENGALI
                : stageIndex == 1 ? EVERYDAY_TOPIC_BENGALI : CONFIDENT_TOPIC_BENGALI;
        String level = stageIndex == 0 ? "Basic" : stageIndex == 1 ? "Everyday" : "Confident Conversation";
        String focus = stageIndex == 0
                ? "Build clear everyday phrases and speak in short, complete sentences."
                : stageIndex == 1
                ? "Connect ideas, handle common situations and ask useful follow-up questions."
                : "Speak with confidence, organise your ideas and respond naturally in conversation.";
        String rolePlay = "Speak for one minute about " + topics.get((dayNumber - 1) % 30).toLowerCase(java.util.Locale.ROOT)
                + ". Give one detail, ask a question and respond to a follow-up.";
        return new DayPlan(dayNumber, level, topics.get((dayNumber - 1) % 30), bengaliTopics.get((dayNumber - 1) % 30), focus, rolePlay,
                "নিজের অভিজ্ঞতা থেকে উদাহরণ দিন, একটি কারণ বলুন এবং অপরজনকে একটি প্রশ্ন করুন।");
    }

    static final class DayPlan {
        final int dayNumber;
        final String level;
        final String title;
        final String bengaliTopic;
        final String focus;
        final String rolePlay;
        final String bengaliRolePlay;

                DayPlan(int dayNumber, String level, String title, String bengaliTopic, String focus, String rolePlay, String bengaliRolePlay) {
            this.dayNumber = dayNumber;
            this.level = level;
            this.title = title;
                        this.bengaliTopic = bengaliTopic;
            this.focus = focus;
            this.rolePlay = rolePlay;
            this.bengaliRolePlay = bengaliRolePlay;
        }
    }
}
