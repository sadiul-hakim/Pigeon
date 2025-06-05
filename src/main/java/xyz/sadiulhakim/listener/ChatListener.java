package xyz.sadiulhakim.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xyz.sadiulhakim.chat.ChatService;
import xyz.sadiulhakim.user.event.DeleteChatEvent;

@Component
class ChatListener {

    private final ChatService chatService;

    ChatListener(ChatService chatService) {
        this.chatService = chatService;
    }

    @Async("taskExecutor")
    @EventListener
    void deleteChats(DeleteChatEvent chatEvent) {
        chatService.deleteAllMessageBetweenTwoUsers(chatEvent.user().getEmail(), chatEvent.toUser().getEmail());
    }
}
