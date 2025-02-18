package xyz.sadiulhakim.user.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import xyz.sadiulhakim.user.model.UserDTO;
import xyz.sadiulhakim.user.model.UserService;
import xyz.sadiulhakim.user.model.User;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
class UserController {
    private final UserService userService;

    @PostMapping("/")
    String saveUserApi(@RequestBody UserDTO user, @RequestParam MultipartFile photo, Model model) {
        userService.save(user, photo);
        model.addAttribute("saved", true);

        return "login";
    }
}
