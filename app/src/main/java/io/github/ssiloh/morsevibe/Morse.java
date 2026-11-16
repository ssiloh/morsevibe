package io.github.ssiloh.morsevibe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** ITU Morse table plus helpers to turn user input into a canonical dot/dash string. */
public final class Morse {
    public static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String DIGITS = "0123456789";

    private static final Map<Character, String> TABLE = new LinkedHashMap<>();

    static {
        String[] letters = {
                ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--",
                "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--.."};
        for (int i = 0; i < 26; i++) TABLE.put((char) ('A' + i), letters[i]);
        String[] digits = {"-----", ".----", "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----."};
        for (int i = 0; i < 10; i++) TABLE.put((char) ('0' + i), digits[i]);
        TABLE.put('.', ".-.-.-");
        TABLE.put(',', "--..--");
        TABLE.put('?', "..--..");
        TABLE.put('\'', ".----.");
        TABLE.put('!', "-.-.--");
        TABLE.put('/', "-..-.");
        TABLE.put('(', "-.--.");
        TABLE.put(')', "-.--.-");
        TABLE.put('&', ".-...");
        TABLE.put(':', "---...");
        TABLE.put(';', "-.-.-.");
        TABLE.put('=', "-...-");
        TABLE.put('+', ".-.-.");
        TABLE.put('-', "-....-");
        TABLE.put('_', "..--.-");
        TABLE.put('"', ".-..-.");
        TABLE.put('$', "...-..-");
        TABLE.put('@', ".--.-.");
    }

    private Morse() {}

    /** Code for a single character, or null if it has none. */
    public static String codeFor(char c) {
        return TABLE.get(Character.toUpperCase(c));
    }

    /**
     * Converts user input into the canonical form: dots and dashes, a single space between letters
     * and " / " between words. Input made only of dots, dashes, spaces and slashes is treated as raw
     * Morse; anything else is treated as text. Returns "" if nothing playable results.
     */
    public static String toCode(String input) {
        if (input == null) return "";
        String s = input.trim();
        if (s.isEmpty()) return "";
        if (isRaw(s)) return normalizeRaw(s);

        StringBuilder out = new StringBuilder();
        for (String word : s.split("\\s+")) {
            StringBuilder w = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                String c = codeFor(word.charAt(i));
                if (c == null) continue;
                if (w.length() > 0) w.append(' ');
                w.append(c);
            }
            if (w.length() == 0) continue;
            if (out.length() > 0) out.append(" / ");
            out.append(w);
        }
        return out.toString();
    }

    /** Splits a canonical code string into words, each a list of letters made of '.' and '-'. */
    public static List<List<String>> parse(String code) {
        List<List<String>> words = new ArrayList<>();
        if (code == null) return words;
        for (String w : code.split("/")) {
            List<String> letters = new ArrayList<>();
            for (String l : w.trim().split("\\s+")) {
                if (!l.isEmpty()) letters.add(l);
            }
            if (!letters.isEmpty()) words.add(letters);
        }
        return words;
    }

    /** Two-column-ish reference chart for the practice screen. */
    public static String chart() {
        StringBuilder sb = new StringBuilder();
        String all = LETTERS + DIGITS;
        for (int i = 0; i < all.length(); i++) {
            char c = all.charAt(i);
            sb.append(c).append("  ").append(String.format("%-6s", TABLE.get(c)));
            sb.append(i % 3 == 2 ? "\n" : "   ");
        }
        return sb.toString().trim();
    }

    private static boolean isRaw(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (" ./-·•–—_".indexOf(s.charAt(i)) < 0 && !Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static String normalizeRaw(String s) {
        String t = s.replace('·', '.').replace('•', '.')
                .replace('–', '-').replace('—', '-').replace('_', '-')
                .replace("/", " / ");
        StringBuilder out = new StringBuilder();
        boolean pendingWord = false;
        for (String tok : t.trim().split("\\s+")) {
            if (tok.isEmpty()) continue;
            if (tok.equals("/")) {
                pendingWord = out.length() > 0;
                continue;
            }
            if (out.length() > 0) out.append(pendingWord ? " / " : " ");
            pendingWord = false;
            out.append(tok);
        }
        return out.toString();
    }
}
