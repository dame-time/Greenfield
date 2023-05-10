package utils.generators;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A utility class for generating random strings.
 */
public final class RandomStringGenerator {
    private static final int MIN_LENGTH = 15;
    private static final int MAX_LENGTH = 30;

    private static final int ASCII_START = 97;
    private static final int ASCII_END = 122;

    private static final Set<String> generatedStrings = new HashSet<>();

    /**
     * Generates a random string with a length between {@value #MIN_LENGTH} and {@value #MAX_LENGTH} characters,
     * using characters from the ASCII table.
     * The method should handle concurrent generation of strings.
     *
     * @return a randomly generated string
     */
    public static String generate() {
        int length = ThreadLocalRandom.current().nextInt(MIN_LENGTH, MAX_LENGTH + 1);

        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            char c = (char) ThreadLocalRandom.current().nextInt(ASCII_START, ASCII_END + 1);
            sb.append(c);
        }

        String generatedString = sb.toString();
        while (generatedStrings.contains(generatedString)) {
            sb = new StringBuilder();
            while (sb.length() < length) {
                char c = (char) ThreadLocalRandom.current().nextInt(ASCII_START, ASCII_END + 1);
                sb.append(c);
            }
            generatedString = sb.toString();
        }
        generatedStrings.add(generatedString);

        return generatedString;
    }
}
