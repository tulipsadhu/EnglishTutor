package com.example.englishvoicecoach;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DailyProgressTest {
    @Test
    public void sentencePassesAtSeventyPercent() {
        assertEquals(0, DailyProgress.markSentencePassed(0, 2, 69));
        assertEquals(4, DailyProgress.markSentencePassed(0, 2, 70));
    }

    @Test
    public void nextSentenceIsFirstUnpassedItem() {
        assertEquals(1, DailyProgress.firstUnpassedSentence(0b00000101, 8));
        assertEquals(8, DailyProgress.firstUnpassedSentence(0b11111111, 8));
    }

    @Test
    public void dayCompletesOnlyAfterEverySentencePasses() {
        assertFalse(DailyProgress.isDayComplete(0b01111111, 8));
        assertTrue(DailyProgress.isDayComplete(0b11111111, 8));
    }
}