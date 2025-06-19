package xyz.sadiulhakim.chat.enumeration;

public enum ChatArea {
    PEOPLE,
    GROUP;

    public static ChatArea of(String area) {
        for (ChatArea value : values()) {
            if (value.name().equalsIgnoreCase(area)) {
                return value;
            }
        }

        return null;
    }
}
