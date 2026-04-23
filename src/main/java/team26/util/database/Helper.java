package team26.util.database;

import java.util.Arrays;

public class Helper {
    public static Integer[] validateAndCopyNumbers(Integer[] numbers) {
        if (numbers == null) {
            throw new IllegalArgumentException("Массив чисел не может быть null");
        }

        if (numbers.length != 5) {
            throw new IllegalArgumentException("Должно быть ровно 5 чисел, получено: " + numbers.length);
        }

        for (Integer num : numbers) {
            if (num == null) {
                throw new IllegalArgumentException("Число не может быть null");
            }

            if (num < 1 || num > 45) {
                throw new IllegalArgumentException("Число " + num + " вне диапазона 1-45");
            }

        }
        long distinctCount = Arrays.stream(numbers).distinct().count();
        if (distinctCount != 5) {
            throw new IllegalArgumentException("Все числа должны быть уникальны");
        }

        return numbers.clone();
    }
}
