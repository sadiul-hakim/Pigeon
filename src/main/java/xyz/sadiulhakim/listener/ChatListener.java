package xyz.sadiulhakim.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xyz.sadiulhakim.chat.model.ChatService;
import xyz.sadiulhakim.user.event.ChatEvent;

@Component
class ChatListener {

    private final ChatService chatService;

    ChatListener(ChatService chatService) {
        this.chatService = chatService;
    }

    @Async("taskExecutor")
    @EventListener
    void deleteChats(ChatEvent chatEvent) {
        chatService.deleteAllMessage(chatEvent.user(), chatEvent.toUser());
    }
}
