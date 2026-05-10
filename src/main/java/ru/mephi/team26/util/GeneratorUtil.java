package ru.mephi.team26.util;

import ru.mephi.team26.entity.Draw;

import java.security.SecureRandom;
import java.util.*;

public class GeneratorUtil {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static List<Integer> generateWinningNumbers(Draw draw) {
        Set<Integer> values = new HashSet<>();
        while (values.size() < draw.getNumbersCount()) {
            values.add(RANDOM.nextInt(draw.getMaxNumber()) + 1);
        }
        List<Integer> result = new ArrayList<>(values);
        result.sort(Comparator.naturalOrder());
        return result;
    }
}
