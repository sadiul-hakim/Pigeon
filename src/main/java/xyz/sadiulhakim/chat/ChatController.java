package xyz.sadiulhakim.chat;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import xyz.sadiulhakim.chat.pojo.ChatSetup;
import xyz.sadiulhakim.user.model.UserService;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private final UserService userService;

    public ChatController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String chat(Model model) {
        ChatSetup chatSetup = userService.getChatSetup();
        model.addAttribute("setup", chatSetup);

        return "chat";
    }
}
