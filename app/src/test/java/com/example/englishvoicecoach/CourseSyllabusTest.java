package com.example.englishvoicecoach;

import org.junit.Test;

import java.util.HashSet;
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
    public void everyDayHasLocalizedTopicAndPracticePrompts() {
        for (int dayNumber = 1; dayNumber <= CourseSyllabus.COURSE_DAYS; dayNumber++) {
            CourseSyllabus.DayPlan plan = CourseSyllabus.forDay(dayNumber);
            assertNotNull(plan.title);
            assertFalse(plan.bengaliTopic.trim().isEmpty());
            assertFalse(plan.bengaliRolePlay.trim().isEmpty());

            List<BeginnerCourse.PracticeSentence> sentences = DailyPracticeFactory.generatedSentences(plan);
            assertEquals(30, sentences.size());
            Set<String> uniqueEnglish = new HashSet<>();
            for (BeginnerCourse.PracticeSentence sentence : sentences) {
                assertFalse(sentence.english.trim().isEmpty());
                assertFalse(sentence.bengali.trim().isEmpty());
                uniqueEnglish.add(sentence.english);
            }
            assertEquals(30, uniqueEnglish.size());
        }
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