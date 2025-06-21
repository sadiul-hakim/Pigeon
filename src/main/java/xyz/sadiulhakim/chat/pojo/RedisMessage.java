package xyz.sadiulhakim.chat.pojo;

import java.util.List;

public record RedisMessage(List<String> recipients, ChatMessage message) {
}

