package xyz.sadiulhakim.user.event;

import xyz.sadiulhakim.user.model.User;

public record ChatEvent(
        User user,
        User toUser
) {
}
