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

    @GetMapping("/register_page")
    String registerPage(Model model) {

        model.addAttribute("user", new UserDTO());
        return "register";
    }

    @GetMapping("/login_page")
    String loginPage() {
        return "login";
    }
}
