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
import xyz.sadiulhakim.group.service.ChatGroupService;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    private final ChatGroupService groupService;

    @GetMapping("/create-group")
    String createGroup(@RequestParam String name) {
        groupService.create(name);
        return "redirect:/chat";
    }

    @GetMapping
    public String chatWithoutUser(Model model) {

        // Handle case when no user is provided
        ChatSetup chatSetup = chatService.getChatSetup(null, null, null);
        model.addAttribute("setup", chatSetup); // or some default
        return "chat";
    }

    @GetMapping("/{selectedUser}/{area}")
    public String chat(
            @PathVariable String selectedUser,
            @PathVariable String selectedGroup,
            @PathVariable String area, Model model
    ) {
        UUID userId = UUID.fromString(selectedUser);
        UUID groupId = UUID.fromString(selectedGroup);
        ChatSetup chatSetup = chatService.getChatSetup(userId, groupId, area);
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
