package com.example.englishvoicecoach;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LearningProgressTest {
    @Test
    public void exactTranscriptMatchesCompletely() {
        assertEquals(100, LearningProgress.wordMatchScore(
                "I am learning English every day.",
                "I am learning English every day"
        ));
    }

    @Test
    public void omittedWordProducesPartialMatch() {
        assertEquals(83, LearningProgress.wordMatchScore(
                "I am learning English every day.",
                "I am learning every day"
        ));
    }

    @Test
    public void reorderedWordsAreNotCountedAsAnExactMatch() {
        assertTrue(LearningProgress.wordMatchScore("I like learning English", "English learning like I") < 70);
    }

    @Test
    public void curriculumMovesFromBasicToAdvanced() {
        assertEquals(12, LearningProgress.LESSONS.size());
        assertEquals("Basic", LearningProgress.LESSONS.get(0).level);
        assertEquals("Advanced", LearningProgress.LESSONS.get(11).level);
    }
}