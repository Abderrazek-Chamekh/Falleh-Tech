package tn.esprit.Controllers.Blog.Comment;

import java.util.*;
import java.util.regex.*;

public class BadWordFilter {
    private static final String REPLACEMENT = "****";
    private static final Set<String> BAD_WORDS = loadBadWords();
    private static final Map<String, String> LEETSPEAK_MAP = createLeetspeakMap();

    private static Set<String> loadBadWords() {
        return new HashSet<>(Arrays.asList(
                // English
                "fuck", "fucks", "fucking", "fucked", "f u c k", "f*ck", "f**k",
                "shit", "shits", "shitty", "sh!t", "s**t",
                "asshole", "ass", "a$$", "a s s", "a55",
                "bitch", "bitches", "b!tch", "b1tch", "biatch",
                "bastard", "dick", "d1ck", "d!ck", "cock", "c0ck", "cawk",
                "pussy", "pus5y", "pussie", "cunt", "cum", "jizz",
                "whore", "w h o r e", "slut", "s l u t",
                "motherfucker", "mother fucker", "mf", "m f", "mofo",
                "damn", "dammit", "goddamn", "hell", "crap",
                "screw you", "eat shit", "suck it",

                // French
                "merde", "putain", "enculé", "encule", "connard", "con", "salope",
                "nique", "nique ta mère", "ntm", "bite", "chatte", "pétasse", "trouduc",
                "foutre", "batard", "enfoiré", "bouffon", "gouine", "pd", "tafiole", "sac à foutre",

                // Arabic transliterations
                "zebi", "kos", "kess", "9a7ba", "qa7ba", "3ayr", "3ir", "nik", "nikmok", "walak",
                "bn k", "klb", "sharmouta", "sharmout", "kos ommak", "ommak", "ya hmar", "ya kalb",
                "kalb", "ya 3ayr", "3ayr", "ya 7mar", "7mar", "ya 3ir", "3ir", "ya zebi", "zebi",

                // Variants & leetspeak
                "f*ucking", "s!ut", "d@mn", "sh!thead", "c*nt", "bi@tch", "douche", "douchebag",
                "twat", "wanker", "prick", "arse", "bollocks", "bugger", "jackass"
        ));
    }

    private static Map<String, String> createLeetspeakMap() {
        Map<String, String> map = new HashMap<>();
        map.put("1", "i");
        map.put("!", "i");
        map.put("@", "a");
        map.put("3", "e");
        map.put("4", "a");
        map.put("5", "s");
        map.put("7", "t");
        map.put("0", "o");
        map.put("9", "g");
        map.put("$", "s");
        map.put("*", "");
        return map;
    }

    public static String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // Normalize the text
        String normalized = normalizeText(text.toLowerCase());

        // Split into words while preserving punctuation
        String[] words = text.split("\\b");
        StringBuilder filtered = new StringBuilder();

        for (String word : words) {
            String normalizedWord = normalizeText(word.toLowerCase());
            if (BAD_WORDS.contains(normalizedWord)) {
                filtered.append(REPLACEMENT);
            } else {
                filtered.append(word);
            }
        }

        return filtered.toString();
    }

    private static String normalizeText(String text) {
        // Convert leetspeak to normal letters
        StringBuilder normalized = new StringBuilder();
        for (char c : text.toCharArray()) {
            String replacement = LEETSPEAK_MAP.get(String.valueOf(c));
            normalized.append(replacement != null ? replacement : c);
        }

        // Remove repeated characters (like "fuuuuck" -> "fuck")
        return normalized.toString()
                .replaceAll("(.)\\1{2,}", "$1");
    }

    public static boolean containsBadWords(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        String normalized = normalizeText(text.toLowerCase());
        String[] words = normalized.split("\\b");

        for (String word : words) {
            if (BAD_WORDS.contains(word)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> detectBadWords(String text) {
        List<String> foundWords = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return foundWords;
        }

        String normalized = normalizeText(text.toLowerCase());
        String[] words = normalized.split("\\b");

        for (String word : words) {
            if (BAD_WORDS.contains(word)) {
                foundWords.add(word);
            }
        }
        return foundWords;
    }
}
