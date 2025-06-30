package xyz.sadiulhakim.group;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.sadiulhakim.chat.pojo.ChatMessage;
import xyz.sadiulhakim.group.service.ChatGroupService;
import xyz.sadiulhakim.group.service.GroupChatService;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final ChatGroupService groupService;
    private final GroupChatService groupChatService;

    @MessageMapping("/sent-group")
    ChatMessage sendMessage(
            @Payload ChatMessage message, Principal principal) throws AccessDeniedException {
        if (principal == null) {
            throw new AccessDeniedException("Unauthorized!");
        }

        message.setUser(principal.getName());
        groupService.sendMessage(message);
        return message;
    }

    @ResponseBody
    @DeleteMapping("/chat/{chatId}")
    ResponseEntity<?> delete(@PathVariable long chatId) {
        var message = groupChatService.delete(chatId);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/create")
    String createGroup(@RequestParam String name) {
        groupService.create(name);
        return "redirect:/chat";
    }

    @PostMapping("/update")
    String createGroup(
            @RequestParam String groupId,
            @RequestParam String name,
            @RequestParam MultipartFile file,
            RedirectAttributes model
    ) {
        UUID group = UUID.fromString(groupId);
        String message = groupService.update(name, file, group);
        model.addFlashAttribute("isGroupAction", true);
        model.addFlashAttribute("groupActionMessage", message);
        return "redirect:/chat/" + groupId + "/" + "GROUP";
    }

    @GetMapping("/remove/{groupId}/{memberId}")
    String removeMember(@PathVariable String groupId, @PathVariable String memberId, RedirectAttributes model) {
        UUID group = UUID.fromString(groupId);
        UUID member = UUID.fromString(memberId);
        String message = groupService.removeFromGroup(group, member);
        model.addFlashAttribute("isGroupAction", true);
        model.addFlashAttribute("groupActionMessage", message);
        return "redirect:/chat/" + groupId + "/" + "GROUP";
    }

    @GetMapping("/add/{groupId}/{memberId}")
    String addMember(@PathVariable String groupId, @PathVariable String memberId, RedirectAttributes model) {
        UUID group = UUID.fromString(groupId);
        UUID member = UUID.fromString(memberId);
        String message = groupService.addToGroup(group, member);
        model.addFlashAttribute("isGroupAction", true);
        model.addFlashAttribute("groupActionMessage", message);
        return "redirect:/chat/" + groupId + "/" + "GROUP";
    }

    @GetMapping("/close/{groupId}")
    String closeGroup(@PathVariable String groupId, RedirectAttributes model) {
        UUID group = UUID.fromString(groupId);
        String message = groupService.closeGroup(group);
        model.addFlashAttribute("isGroupAction", true);
        model.addFlashAttribute("groupActionMessage", message);
        return "redirect:/chat";
    }

    @GetMapping("/leave/{groupId}")
    String leaveGroup(@PathVariable String groupId, RedirectAttributes model) {
        UUID group = UUID.fromString(groupId);
        String message = groupService.leaveGroup(group);
        model.addFlashAttribute("isGroupAction", true);
        model.addFlashAttribute("groupActionMessage", message);
        return "redirect:/chat";
    }

    @GetMapping("/admin/{groupId}/{memberId}/{make}")
    String makeOrRemoveAdmin(
            @PathVariable String groupId,
            @PathVariable String memberId,
            @PathVariable boolean make,
            RedirectAttributes model) {
        UUID group = UUID.fromString(groupId);
        UUID member = UUID.fromString(memberId);
        String message = groupService.makeOrRemoveAdmin(group, member, make);
        model.addFlashAttribute("isGroupAction", true);
        model.addFlashAttribute("groupActionMessage", message);
        return "redirect:/chat/" + groupId + "/" + "GROUP";
    }
}
