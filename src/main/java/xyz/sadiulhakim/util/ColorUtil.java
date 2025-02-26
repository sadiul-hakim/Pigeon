package xyz.sadiulhakim.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ColorUtil {

    private static final List<String> COLORS = List.of(
            "#2F4F4F", // Dark Slate Gray
            "#6A4E42", // Shadow
            "#4B0082", // Indigo
            "#708090", // Slate Gray
            "#8B4513", // Saddle Brown
            "#A9A9A9", // Dark Gray
            "#556B2F", // Dark Olive Green
            "#8B0000", // Dark Red
            "#5D3F6B", // Purple Taupe
            "#3E3B32"  // Dark Taupe
    );

    public static String getRandomColor() {
        return COLORS.get(ThreadLocalRandom.current().nextInt(COLORS.size()));
    }
}
