package xyz.sadiulhakim.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xyz.sadiulhakim.chat.model.ChatService;
import xyz.sadiulhakim.chat.pojo.ChatMessage;
import xyz.sadiulhakim.chat.pojo.ChatSetup;

import java.util.UUID;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public String chat(@RequestParam(defaultValue = "") UUID toUser, Model model) {
        ChatSetup chatSetup = chatService.getChatSetup(toUser);
        model.addAttribute("setup", chatSetup);

        return "chat";
    }

    @MessageMapping("/sent")
    ChatMessage sendMessage(
            @Payload ChatMessage message
    ) {
        chatService.sendMessage(message);
        return message;
    }
}
