package xyz.sadiulhakim.user.event;

import xyz.sadiulhakim.user.User;

public record DeleteChatEvent(
        User user,
        User toUser
) {
}
