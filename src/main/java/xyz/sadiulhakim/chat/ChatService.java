package xyz.sadiulhakim.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.sadiulhakim.chat.pojo.ChatMessage;
import xyz.sadiulhakim.chat.pojo.ChatSetup;
import xyz.sadiulhakim.notification.NotificationService;
import xyz.sadiulhakim.user.User;
import xyz.sadiulhakim.user.pojo.UserDTO;
import xyz.sadiulhakim.user.UserService;
import xyz.sadiulhakim.util.AppProperties;
import xyz.sadiulhakim.util.FileUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepo chatRepo;
    private final UserService userService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AppProperties appProperties;

    public ChatSetup getChatSetup(UUID toUser) {

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return new ChatSetup();
        }

        User user = userService.findByEmail(authentication.getName());
        UserDTO userDTO = userService.convertToDto(user);
        List<UUID> connectedUsers = user.getConnectedUsers();
        List<UserDTO> connections = userService.findAllUserConnections(connectedUsers);

        ChatSetup chatSetup = new ChatSetup();
        chatSetup.setUser(userDTO);
        chatSetup.setConnections(connections);
        chatSetup.setNotifications(notificationService.countByUser(user.getId()));

        if (connections.isEmpty()) {
            chatSetup.setSelectedUser(null);
        } else if (toUser == null) {
            chatSetup.setSelectedUser(connections.getFirst());
        } else {
            Optional<UserDTO> selectedUser = connections.stream()
                    .filter(u -> u.getId().equals(toUser)).findFirst();
            chatSetup.setSelectedUser(selectedUser.orElse(new UserDTO()));
        }

        if (chatSetup.getSelectedUser() != null) {
            List<Chat> chats = findAllChat(chatSetup.getUser().getId(), chatSetup.getSelectedUser().getId());
            chatSetup.setInitialChat(chats);
        } else {
            chatSetup.setInitialChat(new ArrayList<>());
        }

        return chatSetup;
    }

    public void sendMessage(ChatMessage message) {

        User user = userService.findByEmail(message.getUser());
        User toUser = userService.findByEmail(message.getToUser());

        if (user == null || toUser == null)
            return;

        // Handle File
        if (StringUtils.hasText(message.getFileName()) && StringUtils.hasText(message.getFileContent())) {
            try {
                byte[] fileData = Base64.getDecoder().decode(message.getFileContent());
                String filePath = FileUtil.uploadFile(appProperties.getMessageImageFolder(), message.getFileName(), fileData);
                message.setFileName(filePath);
            } catch (Exception e) {
                log.error("sendMessage() :: Could not upload file!");
            }
        }

        // Launch a thread to save the message
        LocalDateTime now = LocalDateTime.now();
        Thread.ofVirtual().name("#ChatThread-", 0).start(() -> {
            save(message.getMessage(), message.getFileName(), user, toUser, now);
        });

        // Prepare and send the message to both users so that they can see on screen
        message.setSendTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(now));
        message.setUserName(user.getLastname());
        message.setUserPicture(user.getPicture());
        message.setUserTextColor(user.getTextColor());

        messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/messages", message);
        messagingTemplate.convertAndSendToUser(toUser.getEmail(), "/queue/messages", message);
    }

    public void save(String message, String fileName, User user, User toUser, LocalDateTime now) {

        if (!StringUtils.hasText(message))
            return;

        Chat chat = new Chat();
        chat.setUser(user);
        chat.setToUser(toUser);
        chat.setMessage(message);
        chat.setSendTime(now);
        chat.setFilename(fileName);

        chatRepo.save(chat);
    }

    public List<Chat> findAllChat(UUID userId, UUID toUserId) {
        Optional<User> user = userService.findById(userId);
        Optional<User> toUser = userService.findById(toUserId);
        if (user.isEmpty() || toUser.isEmpty())
            return Collections.emptyList();

        return chatRepo.findAllByUserAndToUserOrToUserAndUserOrderBySendTime(
                user.get(), toUser.get(),
                user.get(), toUser.get()
        );
    }

    public void deleteAllMessageBetweenTwoUsers(String user, String toUser) {

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        User userModel = userService.findByEmail(authentication.getName());
        if (!(userModel.getEmail().equals(user) || userModel.getEmail().equals(toUser))) {
            return;
        }

        chatRepo.deleteChatBetweenUsers(
                user, toUser
        );
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

        chatRepo.delete(chat);
        return "Successfully deleted the chat message!";
    }
}
