package com.example.englishvoicecoach;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SpeechScoringTest {
    @Test
    public void exactTranscriptMatchesCompletely() {
        assertEquals(100, SpeechScoring.wordMatchScore(
                "I am learning English every day.",
                "I am learning English every day"
        ));
    }

    @Test
    public void omittedWordProducesPartialMatch() {
        assertEquals(83, SpeechScoring.wordMatchScore(
                "I am learning English every day.",
                "I am learning every day"
        ));
    }

    @Test
    public void reorderedWordsAreNotCountedAsAnExactMatch() {
        assertTrue(SpeechScoring.wordMatchScore("I like learning English", "English learning like I") < 70);
    }

    @Test
    public void emptyTranscriptHasNoMatch() {
        assertEquals(0, SpeechScoring.wordMatchScore("Hello there", "  "));
    }
}