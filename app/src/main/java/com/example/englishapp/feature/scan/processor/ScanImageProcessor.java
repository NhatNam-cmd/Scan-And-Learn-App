package com.example.englishapp.feature.scan.processor;

import com.example.englishapp.feature.scan.model.ScanWordItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ScanImageProcessor {

    private static final int MIN_WORD_LENGTH = 2;

    private static final Set<String> STOP_WORDS =
            new HashSet<>(Arrays.asList(

                    "a",
                    "an",
                    "the",
                    "is",
                    "are",
                    "was",
                    "were",
                    "be",
                    "been",
                    "being",
                    "to",
                    "of",
                    "in",
                    "on",
                    "at",
                    "for",
                    "from",
                    "by",
                    "with",
                    "as",
                    "that",
                    "this",
                    "these",
                    "those",
                    "and",
                    "or",
                    "but",
                    "if",
                    "then",
                    "than",
                    "it",
                    "its",
                    "he",
                    "she",
                    "they",
                    "them",
                    "his",
                    "her",
                    "their",
                    "you",
                    "your",
                    "we",
                    "our",
                    "i",
                    "me",
                    "my",
                    "mine"

            ));

    public List<ScanWordItem> extractWords(String rawText) {

        if (rawText == null || rawText.trim().isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Integer> frequencyMap = new HashMap<>();

        String cleaned = rawText

                .replace("\n", " ")

                .replaceAll("[^A-Za-z ]", " ")

                .replaceAll("\\s+", " ")

                .trim();

        if (cleaned.isEmpty()) {
            return Collections.emptyList();
        }

        String[] words = cleaned.split(" ");

        for (String word : words) {

            if (word == null) continue;

            word = word.trim();

            if (word.isEmpty()) continue;

            if (word.length() < MIN_WORD_LENGTH) continue;

            word = word.toLowerCase(Locale.US);

            frequencyMap.put(
                    word,
                    frequencyMap.getOrDefault(word, 0) + 1
            );
        }

        List<ScanWordItem> result = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {

            String word = entry.getKey();

            int frequency = entry.getValue();

            boolean stopWord =
                    STOP_WORDS.contains(word);

            result.add(

                    new ScanWordItem(

                            capitalize(word),

                            false,

                            stopWord,

                            frequency
                    )
            );
        }

        Collections.sort(result, new Comparator<ScanWordItem>() {

            @Override
            public int compare(ScanWordItem o1, ScanWordItem o2) {

                if (o1.isStopWord() != o2.isStopWord()) {

                    return o1.isStopWord() ? 1 : -1;

                }

                if (o1.getFrequency() != o2.getFrequency()) {

                    return Integer.compare(
                            o2.getFrequency(),
                            o1.getFrequency()
                    );
                }

                return o1.getWord().compareToIgnoreCase(o2.getWord());
            }
        });

        return result;
    }

    private String capitalize(String word) {

        if (word == null || word.isEmpty()) {
            return word;
        }

        return word.substring(0, 1).toUpperCase(Locale.US)
                + word.substring(1);
    }

}