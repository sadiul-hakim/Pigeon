package xyz.sadiulhakim.group;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.sadiulhakim.chat.pojo.ChatMessage;
import xyz.sadiulhakim.group.service.ChatGroupService;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final ChatGroupService groupService;

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

    @GetMapping("/create")
    String createGroup(@RequestParam String name) {
        groupService.create(name);
        return "redirect:/chat";
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
