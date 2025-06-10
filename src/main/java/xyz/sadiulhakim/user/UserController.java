package xyz.sadiulhakim.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.sadiulhakim.user.ConnectionRequest;
import xyz.sadiulhakim.user.User;
import xyz.sadiulhakim.user.UserService;
import xyz.sadiulhakim.user.pojo.PasswordDTO;
import xyz.sadiulhakim.util.PaginationResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
class UserController {

    private final UserService userService;

    @GetMapping("/search")
    String searchUser(@RequestParam String text, Model model) {
        PaginationResult paginationResult = userService.searchUser(text, 0, true);
        model.addAttribute("result", paginationResult);
        return "display_users";
    }

    @PostMapping("/change_picture")
    String changeProfilePicture(@RequestParam MultipartFile photo, @RequestParam UUID userId) {
        userService.updateProfilePicture(photo, userId);
        return "redirect:/user/profile";
    }

    @PostMapping("/update_profile")
    String updateProfile(@ModelAttribute User user) {
        userService.update(user);
        return "redirect:/user/profile";
    }

    @GetMapping("/send-connect-request")
    @ResponseBody
    ResponseEntity<?> connect(@RequestParam UUID toUser) {
        String connect = userService.connect(toUser);
        return ResponseEntity.ok(Collections.singletonMap("message", connect));
    }

    @GetMapping("/remove-connection")
    @ResponseBody
    ResponseEntity<?> removeConnection(@RequestParam UUID toUser) {
        String connect = userService.removeConnection(toUser);
        return ResponseEntity.ok(Collections.singletonMap("message", connect));
    }

    @GetMapping("/cancel-connect-request")
    @ResponseBody
    ResponseEntity<?> cancelConnection(@RequestParam long requestId) {
        String connect = userService.cancelConnection(requestId);
        return ResponseEntity.ok(Collections.singletonMap("message", connect));
    }

    @GetMapping("/accept-connect-request")
    @ResponseBody
    ResponseEntity<?> acceptConnection(@RequestParam long requestId) {
        String connect = userService.acceptConnection(requestId);
        return ResponseEntity.ok(Collections.singletonMap("message", connect));
    }

    @GetMapping("/sent_connections_page")
    String sentConnectionsPage(Model model) {

        List<ConnectionRequest> requests = userService.getAllSentConnectionRequests();
        model.addAttribute("requests", requests);
        return "sentRequests";
    }

    @GetMapping("/received_connections_page")
    String receivedConnectionPage(Model model) {

        List<ConnectionRequest> requests = userService.receivedConnectionRequests();
        model.addAttribute("requests", requests);
        return "receivedRequests";
    }

    @GetMapping("/connections_page")
    String connectionsPage(Model model) {

        var connections = userService.connections();
        model.addAttribute("result", connections);
        return "connections";
    }

    @GetMapping("/profile")
    String profile_page(Model model) {

        Optional<User> user = userService.currentUser();
        model.addAttribute("user", user.orElseGet(User::new));
        return "profile";
    }

    @GetMapping("/change_password_page")
    String changePasswordPage(Model model) {
        model.addAttribute("dto", new PasswordDTO());
        return "change_password";
    }

    @PostMapping("/change_password")
    String changePassword(@ModelAttribute @Valid PasswordDTO dto, BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("dto", dto);
            return "change_password";
        }

        String message = userService.changePassword(dto);
        model.addAttribute("dto", dto);
        model.addAttribute("message", message);
        model.addAttribute("changed", true);
        return "change_password";
    }
}
