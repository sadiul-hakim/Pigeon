package xyz.sadiulhakim.user.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import xyz.sadiulhakim.user.model.UserService;
import xyz.sadiulhakim.user.model.User;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
class UserController {
    private final UserService userService;

    @PostMapping("/")
    String saveUserApi(@RequestBody User user, Model model) {
        userService.save(user);
        model.addAttribute("saved", true);

        return "login";
    }
}
