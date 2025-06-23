package xyz.sadiulhakim.chat.pojo;

import xyz.sadiulhakim.chat.enumeration.ChatArea;

import java.util.List;

public record RedisMessage(
        List<String> recipients,
        String groupChannel,
        ChatArea area,
        ChatMessage message
) {

    public static RedisMessage forPrivateRecipients(List<String> recipients, ChatArea area, ChatMessage message) {
        return new RedisMessage(recipients, null, area, message);
    }

    public static RedisMessage forGroupChannel(String channel, ChatArea area, ChatMessage message) {
        return new RedisMessage(null, channel, area, message);
    }
}


