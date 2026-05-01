package ru.mephi.team26.util;

import java.security.SecureRandom;
import java.util.*;

public class GeneratorUtil {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static List<Integer> generateWinningNumbers(int numbersCount, int maxNumber) {
        Set<Integer> values = new HashSet<>();
        while (values.size() < numbersCount) {
            values.add(RANDOM.nextInt(maxNumber) + 1);
        }
        List<Integer> result = new ArrayList<>(values);
        result.sort(Comparator.naturalOrder());
        return result;
    }
}
