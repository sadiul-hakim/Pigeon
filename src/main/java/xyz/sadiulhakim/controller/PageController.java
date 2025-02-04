package xyz.sadiulhakim.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import xyz.sadiulhakim.user.model.UserDTO;

@Controller
class PageController {

    @GetMapping("/")
    String home() {
        return "index";
    }

    @GetMapping("/register")
    String register(Model model) {

        model.addAttribute("user", new UserDTO());
        return "register";
    }

    @GetMapping("/login")
    String login() {
        return "login";
    }
}
