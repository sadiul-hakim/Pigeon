package xyz.sadiulhakim.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.sadiulhakim.chat.model.ChatService;
import xyz.sadiulhakim.chat.pojo.ChatSetup;
import xyz.sadiulhakim.util.AuthenticationUtil;

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
        model.addAttribute("user", AuthenticationUtil.authenticatedUserId());

        if (toUser == null && chatSetup.getSelectedUser() != null) {
            model.addAttribute("toUser", chatSetup.getSelectedUser().getId());
        } else {
            model.addAttribute("toUser", toUser);
        }

        return "chat";
    }

    @PostMapping("/sent")
    String sendMessage(
            @RequestParam(defaultValue = "") String message,
            @RequestParam UUID user,
            @RequestParam UUID toUser
    ) {
        chatService.save(message, user, toUser);
        return "redirect:/chat";
    }
}
