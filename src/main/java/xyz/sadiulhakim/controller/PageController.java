package xyz.sadiulhakim.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.sadiulhakim.user.model.UserDTO;
import xyz.sadiulhakim.user.model.UserService;

@Controller
@RequiredArgsConstructor
class PageController {

    private final UserService userService;

    @GetMapping("/")
    String home() {
        return "index";
    }

    @GetMapping("/register_page")
    String registerPage(Model model) {

        model.addAttribute("user", new UserDTO());
        return "register";
    }


    @PostMapping("/register")
    String saveUserApi(@ModelAttribute UserDTO user, RedirectAttributes model) {

        userService.save(user);
        model.addFlashAttribute("registered", true);
        model.addFlashAttribute("message", "Registration is complete please login!");

        return "redirect:/login_page";
    }

    @GetMapping("/login_page")
    String loginPage() {
        return "login";
    }
}
