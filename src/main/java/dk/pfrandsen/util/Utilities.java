package dk.pfrandsen.util;

import java.util.ArrayList;
import java.util.List;

public class Utilities {

    public static String join(String separator, List<String> values) {
        StringBuilder builder = new StringBuilder();
        int counter = 0;
        for (String value : values) {
            counter++;
            builder.append(value);
            if (counter != values.size()) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    public static List<String> splitOnUppercase(String text) {
        List<String> words = new ArrayList<String>();
        if (text.length() <= 1) {
            words.add(text);
        } else {
            int startOfWord = 0;
            for (int idx = 1; idx < text.length(); idx++) {
                if (Character.isUpperCase(text.charAt(idx))) {
                    words.add(text.substring(startOfWord, idx));
                    startOfWord = idx;
                }
            }
            words.add(text.substring(startOfWord));
        }
        return words;
    }

    // word is only allowed to contain a sequence of characters a-z followed by an optional sequence of digits
    // all letters must be lowercase except the first which must be uppercase if startWithUppercase is true
    private static boolean checkWord(String word, boolean startWithUppercase) {
        if (word.length() == 0) {
            return false;
        }
        if (startWithUppercase) {
            return word.matches("[A-Z][a-z]*[0-9]*");
        }
        return word.matches("[a-z]+[0-9]*");
    }

    public static boolean isCamelCaseAscii(String text, boolean upper) {
        List<String> wordList = splitOnUppercase(text);
        if (!wordList.isEmpty()) {
            for (int idx = 0; idx < wordList.size(); idx++) {
                String word = wordList.get(idx);
                boolean startWithUppercase = upper || (idx != 0);
                if (!checkWord(word, startWithUppercase)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isUpperCamelCaseAscii(String text) {
        return isCamelCaseAscii(text, true);
    }
    public static boolean isLowerCamelCaseAscii(String text) {
        return isCamelCaseAscii(text, false);
    }

}
