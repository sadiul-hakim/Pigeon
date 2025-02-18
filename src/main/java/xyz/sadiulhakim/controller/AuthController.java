package xyz.sadiulhakim.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.sadiulhakim.user.model.UserDTO;
import xyz.sadiulhakim.user.model.UserService;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/register")
    String register(@ModelAttribute UserDTO user, RedirectAttributes model) {

        user.setRole("ROLE_USER");
        userService.save(user, null);

        model.addFlashAttribute("registered", true);
        model.addFlashAttribute("message", "Registration is complete please login!");

        return "redirect:/login_page";
    }
}
