package xyz.sadiulhakim.util;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ListUtil {
    private ListUtil() {
    }

    public static <T> T findOrDefault(List<T> list, UUID id, Predicate<T> predicate, Supplier<T> fallback) {
        if (list.isEmpty()) return null;
        if (id == null) return list.getFirst();
        return list.stream().filter(predicate).findFirst().orElseGet(fallback);
    }
}
