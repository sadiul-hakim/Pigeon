package xyz.sadiulhakim.group.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import xyz.sadiulhakim.chat.ChatService;
import xyz.sadiulhakim.chat.pojo.ChatSetup;
import xyz.sadiulhakim.group.service.ChatGroupService;

@Controller
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {
    private final ChatGroupService groupService;
    private final ChatService chatService;

    @GetMapping("/create/{name}")
    String createGroup(@PathVariable String name, Model model) {
        groupService.create(name);
        ChatSetup chatSetup = chatService.getChatSetup(null);
        model.addAttribute("setup", chatSetup); // or some default
        return "chat";
    }
}
