package xyz.sadiulhakim.user.event;

import java.util.UUID;

public record ConnectionEvent(
        String message,
        UUID user
) {
}
