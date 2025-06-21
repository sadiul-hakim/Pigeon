package xyz.sadiulhakim.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import xyz.sadiulhakim.chat.enumeration.ChatArea;
import xyz.sadiulhakim.chat.pojo.ChatMessage;
import xyz.sadiulhakim.chat.pojo.ChatSetup;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
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
        ChatSetup chatSetup = chatService.getChatSetup(null, null, null);
        model.addAttribute("setup", chatSetup); // or some default
        return "chat";
    }

    @GetMapping("/{selectedItem}/{area}")
    public String chat(
            @PathVariable String selectedItem,
            @PathVariable String area,
            Model model
    ) {
        ChatSetup chatSetup = null;
        ChatArea chatArea = ChatArea.of(area);
        UUID itemId = UUID.fromString(selectedItem);
        assert chatArea != null;

        // User can use only one area at a time.
        // Meaning either can send message personally or in group
        if (chatArea.equals(ChatArea.PEOPLE)) {
            chatSetup = chatService.getChatSetup(itemId, null, area);
        } else if (chatArea.equals(ChatArea.GROUP)) {
            chatSetup = chatService.getChatSetup(null, itemId, area);
        }
        model.addAttribute("setup", chatSetup);
        return "chat";
    }

    @MessageMapping("/sent")
    ChatMessage sendMessage(
            @Payload ChatMessage message, Principal principal) throws AccessDeniedException {
        if (principal == null) {
            throw new AccessDeniedException("Unauthorized!");
        }

        message.setUser(principal.getName());
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
