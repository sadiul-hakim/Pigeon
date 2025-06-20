package xyz.sadiulhakim.group.event;

import java.util.UUID;

public record GroupEvent(
        String message,
        UUID user
) {
}
