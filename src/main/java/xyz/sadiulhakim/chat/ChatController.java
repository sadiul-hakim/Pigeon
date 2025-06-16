package xyz.sadiulhakim.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import xyz.sadiulhakim.chat.pojo.ChatMessage;
import xyz.sadiulhakim.chat.pojo.ChatSetup;

import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public String chatWithoutUser(Model model) {

        // Handle case when no user is provided
        ChatSetup chatSetup = chatService.getChatSetup(null);
        model.addAttribute("setup", chatSetup); // or some default
        return "chat";
    }

    @GetMapping("/{toUser}")
    public String chat(@PathVariable String toUser, Model model) {
        UUID userId = UUID.fromString(toUser);
        ChatSetup chatSetup = chatService.getChatSetup(userId);
        model.addAttribute("setup", chatSetup);
        return "chat";
    }

    @MessageMapping("/sent")
    ChatMessage sendMessage(
            @Payload ChatMessage message) {

        chatService.sendMessage(message);
        return message;
    }

    @ResponseBody
    @DeleteMapping("/{chatId}")
    ResponseEntity<?> delete(@PathVariable long chatId) {
        String message = chatService.delete(chatId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping("/user/{user}/toUser/{toUser}")
    String delete(@PathVariable String user, @PathVariable String toUser, @RequestParam String selectedUser) {
        chatService.deleteAllMessageBetweenTwoUsers(user, toUser);
        return "redirect:/chat/" + selectedUser;
    }
}
