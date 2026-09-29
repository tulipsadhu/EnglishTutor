package com.example.englishvoicecoach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class SpeechScoring {
    private SpeechScoring() {}

    static int wordMatchScore(String expected, String actual) {
        List<String> expectedWords = words(expected);
        List<String> actualWords = words(actual);
        if (expectedWords.isEmpty() || actualWords.isEmpty()) return 0;

        int[] previousRow = new int[actualWords.size() + 1];
        for (String expectedWord : expectedWords) {
            int[] currentRow = new int[actualWords.size() + 1];
            for (int actualIndex = 1; actualIndex <= actualWords.size(); actualIndex++) {
                if (expectedWord.equals(actualWords.get(actualIndex - 1))) {
                    currentRow[actualIndex] = previousRow[actualIndex - 1] + 1;
                } else {
                    currentRow[actualIndex] = Math.max(previousRow[actualIndex], currentRow[actualIndex - 1]);
                }
            }
            previousRow = currentRow;
        }
        return previousRow[actualWords.size()] * 100 / expectedWords.size();
    }

    private static List<String> words(String text) {
        String normalized = text.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}']+", " ").trim();
        if (normalized.isEmpty()) return Collections.emptyList();
        String[] splitWords = normalized.split("\\s+");
        List<String> result = new ArrayList<>();
        Collections.addAll(result, splitWords);
        return result;
    }
}