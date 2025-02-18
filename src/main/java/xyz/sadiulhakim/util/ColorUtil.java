package xyz.sadiulhakim.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ColorUtil {

    private static final List<String> COLORS = List.of(
            "#191970", // Midnight Blue
            "#005F5F", // Deep Teal
            "#008B8B", // Dark Cyan
            "#4B0082", // Indigo
            "#4169E1", // Royal Blue
            "#000080", // Navy Blue
            "#0F52BA", // Sapphire
            "#003153", // Prussian Blue
            "#9932CC", // Dark Orchid
            "#DDA0DD", // Plum
            "#36013F", // Deep Purple
            "#614051", // Eggplant
            "#800020", // Burgundy
            "#800000", // Maroon
            "#8B008B", // Dark Magenta
            "#6F2DA8", // Grape
            "#C154C1", // Deep Fuchsia
            "#2E1A47", // Violet Indigo
            "#228B22", // Forest Green
            "#095859", // Deep Sea Green
            "#01796F", // Pine Green
            "#8A9A5B", // Moss Green
            "#006A4E", // Bottle Green
            "#556B2F", // Dark Olive Green
            "#355E3B", // Hunter Green
            "#367588", // Teal Blue
            "#4682B4", // Steel Blue
            "#2C3539", // Gunmetal Gray
            "#36454F", // Charcoal Gray
            "#5C4033", // Deep Brown
            "#C04000", // Mahogany
            "#6F4E37", // Coffee Brown
            "#954535", // Chestnut
            "#CD7F32", // Bronze
            "#B87333", // Deep Copper
            "#00CED1", // Dark Turquoise
            "#00BFFF", // Deep Sky Blue
            "#5F9EA0", // Cadet Blue
            "#6A5ACD"  // Slate Blue
    );

    public static String getRandomColor() {
        return COLORS.get(ThreadLocalRandom.current().nextInt(COLORS.size()));
    }
}
