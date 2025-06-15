package com.been.noitu.utils;

import com.been.noitu.NoiTuPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class WordManager {

    private final NoiTuPlugin plugin;
    private final Set<String> dictionary = new HashSet<>();

    public WordManager(NoiTuPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadWords() throws IOException {
        dictionary.clear();
        InputStream is = plugin.getResource("words.txt");
        if (is == null) {
            throw new IOException("Không tìm thấy file words.txt trong file jar!");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    dictionary.add(line.trim().toLowerCase());
                }
            }
        }
        plugin.getLogger().info("Đã tải " + dictionary.size() + " từ vào từ điển.");
    }
    
    public boolean isValidWord(String word) {
        return word.split("\\s+").length == 2 && dictionary.contains(word.toLowerCase());
    }

    public boolean isValidChain(String lastWord, String newWord) {
        String lastSyllable = getLastSyllable(lastWord);
        String firstSyllable = getFirstSyllable(newWord);
        return lastSyllable.equalsIgnoreCase(firstSyllable);
    }

    public String findNextWord(String lastWord) {
        String requiredSyllable = getLastSyllable(lastWord);
        List<String> possibleWords = new ArrayList<>();
        for (String word : dictionary) {
            if (getFirstSyllable(word).equalsIgnoreCase(requiredSyllable)) {
                possibleWords.add(word);
            }
        }

        if (possibleWords.isEmpty()) {
            return null;
        }

        return possibleWords.get(ThreadLocalRandom.current().nextInt(possibleWords.size()));
    }

    public String getFirstSyllable(String word) {
        return word.split("\\s+")[0];
    }
    
    public String getLastSyllable(String word) {
        String[] parts = word.split("\\s+");
        return parts[parts.length - 1];
    }
}