package com.example.englishvoicecoach;

import org.junit.Test;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CourseSyllabusTest {
    @Test
    public void syllabusHasThreeThirtyDayStages() {
        assertEquals(90, CourseSyllabus.COURSE_DAYS);
        assertEquals("Basic", CourseSyllabus.forDay(1).level);
        assertEquals("Basic", CourseSyllabus.forDay(30).level);
        assertEquals("Everyday", CourseSyllabus.forDay(31).level);
        assertEquals("Everyday", CourseSyllabus.forDay(60).level);
        assertEquals("Confident Conversation", CourseSyllabus.forDay(61).level);
        assertEquals("Confident Conversation", CourseSyllabus.forDay(90).level);
    }

    @Test
    public void everyDayHasLocalizedTopicAndThirtyUniquePracticePrompts() {
        List<BeginnerCourse.Day> courseDays = sampleCourseDays();
        for (int dayNumber = 1; dayNumber <= CourseSyllabus.COURSE_DAYS; dayNumber++) {
            CourseSyllabus.DayPlan plan = CourseSyllabus.forDay(dayNumber);
            assertNotNull(plan.title);
            assertFalse(plan.bengaliTopic.trim().isEmpty());
            assertFalse(plan.bengaliRolePlay.trim().isEmpty());

            List<DailyPracticeFactory.Prompt> prompts = DailyPracticeFactory.forDay(dayNumber, courseDays);
            assertEquals(30, prompts.size());
            Set<String> uniqueEnglish = new HashSet<>();
            int newPromptCount = 0;
            for (DailyPracticeFactory.Prompt prompt : prompts) {
                assertFalse(prompt.sentence.english.trim().isEmpty());
                assertFalse(prompt.sentence.bengali.trim().isEmpty());
                uniqueEnglish.add(prompt.sentence.english);
                if (prompt.newToday) newPromptCount++;
            }
            assertEquals(30, uniqueEnglish.size());
            assertEquals(10, newPromptCount);
        }
    }

    private static List<BeginnerCourse.Day> sampleCourseDays() {
        List<BeginnerCourse.Day> days = new ArrayList<>();
        for (int dayNumber = 1; dayNumber <= CourseSyllabus.COURSE_DAYS; dayNumber++) {
            CourseSyllabus.DayPlan plan = CourseSyllabus.forDay(dayNumber);
            List<BeginnerCourse.PracticeSentence> sentences = new ArrayList<>();
            int sentenceCount = dayNumber <= 14 ? 30 : 10;
            for (int sentenceIndex = 1; sentenceIndex <= sentenceCount; sentenceIndex++) {
                sentences.add(new BeginnerCourse.PracticeSentence(
                        "Day " + dayNumber + " sentence " + sentenceIndex + ".",
                        "দিন " + dayNumber + " বাক্য " + sentenceIndex + "।"
                ));
            }
            days.add(new BeginnerCourse.Day(dayNumber, plan.title, plan.focus, plan.rolePlay,
                    plan.bengaliRolePlay, Collections.emptyList(), sentences));
        }
        return days;
    }

    @Test
    public void dailyVolumeUsesTenNewAndTwentyReviewTurns() {
        assertEquals(10, CourseSyllabus.NEW_SENTENCES_PER_DAY);
        assertEquals(20, CourseSyllabus.DAILY_REVIEW_PROMPTS);
        assertEquals(30, CourseSyllabus.DAILY_SPEAKING_TURNS);
        assertTrue(CourseSyllabus.BASIC_TOPICS.size() == 30);
        assertTrue(CourseSyllabus.EVERYDAY_TOPICS.size() == 30);
        assertTrue(CourseSyllabus.CONFIDENT_TOPICS.size() == 30);
    }
}