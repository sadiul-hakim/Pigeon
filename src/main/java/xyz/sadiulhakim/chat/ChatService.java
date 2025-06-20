package xyz.sadiulhakim.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.sadiulhakim.chat.enumeration.ChatArea;
import xyz.sadiulhakim.chat.pojo.ChatMessage;
import xyz.sadiulhakim.chat.pojo.ChatSetup;
import xyz.sadiulhakim.group.ChatGroup;
import xyz.sadiulhakim.group.GroupMember;
import xyz.sadiulhakim.group.repository.GroupMemberRepository;
import xyz.sadiulhakim.group.service.ChatGroupService;
import xyz.sadiulhakim.notification.NotificationService;
import xyz.sadiulhakim.user.User;
import xyz.sadiulhakim.user.UserService;
import xyz.sadiulhakim.user.pojo.UserDTO;
import xyz.sadiulhakim.util.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepo chatRepo;
    private final UserService userService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AppProperties appProperties;
    private final ChatGroupService chatGroupService;
    private final GroupMemberRepository groupMemberRepository;

    public ChatSetup getChatSetup(UUID selectedUser, UUID selectedGroup, String area) {

        // 1. Auth Validation
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return new ChatSetup(); // unauthenticated fallback
        }

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            // 2. Run major I/O tasks in parallel virtual threads
            Future<User> userFuture = executor.submit(() -> userService.findByEmail(authentication.getName()));

            // Wait for user first, as we need their ID for other queries
            User user = userFuture.get(); // Fast, safe on Loom virtual threads
            UUID userId = user.getId();
            List<UUID> connectedUsers = user.getConnectedUsers();

            Future<List<UserDTO>> connectionsFuture = executor.submit(() -> userService.findAllUserConnections(connectedUsers));
            Future<Long> notificationsFuture = executor.submit(() -> notificationService.countByUser(userId));
            Future<List<ChatGroup>> groupsFuture = executor.submit(() -> chatGroupService.joinedGroups(userId));

            // 3. Wait for results
            List<UserDTO> connections = connectionsFuture.get();
            long notifications = notificationsFuture.get();
            List<ChatGroup> groups = groupsFuture.get();

            // 4. Post-process connections
            for (UserDTO connection : connections) {
                connection.setLastSeenText(DateUtil.getLastSeenTime(connection.getLastSeen()));
            }

            // 5. Build ChatSetup
            ChatSetup chatSetup = new ChatSetup();
            chatSetup.setUser(userService.convertToDto(user));
            chatSetup.setConnections(connections);
            chatSetup.setNotifications(notifications);
            chatSetup.setGroups(groups);
            chatSetup.setArea(StringUtils.hasText(area) ? ChatArea.of(area) : ChatArea.PEOPLE);

            // 6. Set selected user
            chatSetup.setSelectedUser(
                    ListUtil.findOrDefault(connections, selectedUser, u -> u.getId().equals(selectedUser), UserDTO::new)
            );

            // 7. Set selected group
            ChatGroup selectedGroupObj = ListUtil.findOrDefault(groups, selectedGroup, g -> g.getId().equals(selectedGroup), ChatGroup::new);
            chatSetup.setSelectedGroup(selectedGroupObj);

            // 8. Current user membership to the selected group
            if (selectedGroupObj != null) {
                Optional<GroupMember> currentUserMemberShip = groupMemberRepository.findByGroupIdAndUserId(selectedGroupObj.getId(), userId);
                chatSetup.setUserMembershipInSelectedGroup(currentUserMemberShip.orElse(new GroupMember()));
            }

            // 9. Initial Chat
            if (chatSetup.getSelectedUser() != null) {
                List<Chat> chats = findAllChat(userId, chatSetup.getSelectedUser().getId());
                chatSetup.setInitialChat(chats);
            } else {
                chatSetup.setInitialChat(Collections.emptyList());
            }

            return chatSetup;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to build chat setup", e);
        }
    }


    public void sendMessage(ChatMessage message) {

        User user = userService.findByEmail(message.getUser());
        User toUser = userService.findByEmail(message.getToUser());

        if (user == null || toUser == null)
            return;

        LocalDateTime now = LocalDateTime.now();

        // Handle File
        if (StringUtils.hasText(message.getFileName()) && StringUtils.hasText(message.getFileContent())) {
            try {
                byte[] fileData = Base64.getDecoder().decode(message.getFileContent());
                String uniqueFileName = FileUtil.getUniqueFileName(message.getFileName(), 20);
                message.setFileName(uniqueFileName);
                FileUtil.uploadFile(appProperties.getMessageImageFolder(), uniqueFileName, fileData);
            } catch (Exception e) {
                log.error("sendMessage() :: Could not upload file!");
            }
        }

        String html = MarkdownUtils.toHtml(message.getMessage());
        message.setMessage(html);

        Chat save = save(message.getMessage(), message.getFileName(), user, toUser, now);
        message.setId(save.getId());

        // Prepare and send the message to both users so that they can see on screen
        message.setSendTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(now));
        message.setUserName(user.getLastname());
        message.setUserPicture(user.getPicture());
        message.setUserTextColor(user.getTextColor());

        // empty the content
        message.setFileContent("");

        messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/messages", message);
        messagingTemplate.convertAndSendToUser(toUser.getEmail(), "/queue/messages", message);
    }

    public Chat save(String message, String fileName, User user, User toUser, LocalDateTime now) {

        if (!StringUtils.hasText(message))
            return new Chat();

        Chat chat = new Chat();
        chat.setUser(user);
        chat.setToUser(toUser);
        chat.setMessage(message);
        chat.setSendTime(now);
        chat.setFilename(fileName);

        return chatRepo.save(chat);
    }

    public List<Chat> findAllChat(UUID userId, UUID toUserId) {
        Optional<User> user = userService.findById(userId);
        Optional<User> toUser = userService.findById(toUserId);
        if (user.isEmpty() || toUser.isEmpty())
            return Collections.emptyList();

        return chatRepo.findConversation(user.get(), toUser.get());
    }

    public void deleteAllMessageBetweenTwoUsers(String user, String toUser) {

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        User userModel = userService.findByEmail(authentication.getName());
        if (!(userModel.getEmail().equals(user) || userModel.getEmail().equals(toUser))) {
            return;
        }

        User toUserModel = userService.findByEmail(toUser);
        List<Chat> chats = chatRepo.findConversation(userModel, toUserModel);
        for (Chat chat : chats) {

            // Delete the file if there is any
            if (StringUtils.hasText(chat.getFilename())) {
                Thread.ofVirtual().name("#FileDeletingThread").start(() -> {
                    FileUtil.deleteFile(appProperties.getMessageImageFolder(), chat.getFilename());
                });
            }
        }

        chatRepo.deleteAll(chats);
    }

    public String delete(long chatId) {
        Optional<Chat> chatOpt = chatRepo.findById(chatId);
        if (chatOpt.isEmpty()) {
            log.warn("ChatService.delete :: Could not find chat with id {}", chatId);
            return "Could not find the chat!";
        }

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        String username = authentication.getName();
        Chat chat = chatOpt.get();
        if (!(username.equals(chat.getUser().getEmail()) || username.equals(chat.getToUser().getEmail()))) {
            return "You are not allowed to delete this chat!";
        }

        // Delete the file if there is any
        if (StringUtils.hasText(chat.getFilename())) {
            Thread.ofVirtual().name("#FileDeletingThread").start(() -> {
                FileUtil.deleteFile(appProperties.getMessageImageFolder(), chat.getFilename());
            });
        }

        chatRepo.delete(chat);
        return "Successfully deleted the chat message!";
    }
}
