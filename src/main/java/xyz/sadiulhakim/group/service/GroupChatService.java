package xyz.sadiulhakim.group.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.sadiulhakim.group.ChatGroup;
import xyz.sadiulhakim.group.GroupChat;
import xyz.sadiulhakim.group.GroupMember;
import xyz.sadiulhakim.group.repository.ChatGroupRepository;
import xyz.sadiulhakim.group.repository.GroupChatRepository;
import xyz.sadiulhakim.group.repository.GroupMemberRepository;
import xyz.sadiulhakim.user.User;
import xyz.sadiulhakim.user.UserService;
import xyz.sadiulhakim.util.AppProperties;
import xyz.sadiulhakim.util.FileUtil;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final GroupChatRepository groupChatRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final UserService userService;
    private final GroupMemberRepository groupMemberRepository;
    private final AppProperties appProperties;

    public GroupChat save(ChatGroup group, GroupMember member, String message, String fileName, LocalDateTime sendTime) {
        GroupChat chat = new GroupChat();
        chat.setGroup(group);
        chat.setSendTime(sendTime);
        chat.setFilename(fileName);
        chat.setSender(member);
        chat.setMessage(message);
        return groupChatRepository.save(chat);
    }

    public List<GroupChat> findAllChat(UUID groupId) {

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        User userModel = userService.findByEmail(authentication.getName());
        Optional<GroupMember> member = groupMemberRepository.findByGroupIdAndUserId(groupId, userModel.getId());
        if (member.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<ChatGroup> group = chatGroupRepository.findById(groupId);
        if (group.isEmpty())
            return Collections.emptyList();

        return groupChatRepository.findAllByGroup(group.get());
    }

    public void deleteAllChat(UUID groupId) {

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        User userModel = userService.findByEmail(authentication.getName());
        Optional<GroupMember> member = groupMemberRepository.findByGroupIdAndUserId(groupId, userModel.getId());
        if (member.isEmpty()) {
            return;
        }

        List<GroupChat> chats = findAllChat(groupId);
        for (GroupChat chat : chats) {

            // Delete the file if there is any
            if (StringUtils.hasText(chat.getFilename())) {
                Thread.ofVirtual().name("#FileDeletingThread").start(() ->
                        FileUtil.deleteFile(appProperties.getGroupMessageImageFolder(), chat.getFilename())
                );
            }
        }

        groupChatRepository.deleteAll(chats);
    }

    public Map<String, Object> delete(long chatId) {
        Optional<GroupChat> chatOpt = groupChatRepository.findById(chatId);
        if (chatOpt.isEmpty()) {
            log.warn("ChatService.delete :: Could not find chat with id {}", chatId);
            return Map.of("message", "Could not find the chat!", "success", false);
        }

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        String username = authentication.getName();
        GroupChat chat = chatOpt.get();
        if (!(chat.getSender().getUser().getEmail().equals(username))) {
            return Map.of("message", "You are not allowed to delete this chat!", "success", false);
        }

        // Delete the file if there is any
        if (StringUtils.hasText(chat.getFilename())) {
            Thread.ofVirtual().name("#FileDeletingThread").start(() -> FileUtil.deleteFile(appProperties.getMessageImageFolder(), chat.getFilename()));
        }

        groupChatRepository.delete(chat);
        return Map.of("message", "Successfully deleted the chat message!", "success", true);
    }
}
