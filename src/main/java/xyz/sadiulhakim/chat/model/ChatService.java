package xyz.sadiulhakim.chat.model;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.sadiulhakim.chat.pojo.ChatSetup;
import xyz.sadiulhakim.notification.model.NotificationService;
import xyz.sadiulhakim.user.model.User;
import xyz.sadiulhakim.user.model.UserDTO;
import xyz.sadiulhakim.user.model.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepo chatRepo;
    private final UserService userService;
    private final NotificationService notificationService;

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
        chatSetup.setNotifications(notificationService.count());

        if (connections.isEmpty()) {
            chatSetup.setSelectedUser(new UserDTO());
        } else if (toUser == null) {
            chatSetup.setSelectedUser(connections.getFirst());
        } else {
            Optional<UserDTO> selectedUser = connections.stream()
                    .filter(u -> u.getId().equals(toUser)).findFirst();
            chatSetup.setSelectedUser(selectedUser.orElse(new UserDTO()));
        }

        List<Chat> chats = findAllChat(chatSetup.getUser().getId(), chatSetup.getSelectedUser().getId());
        chatSetup.setInitialChat(chats);

        return chatSetup;
    }

    public void sendMessage(String message, UUID userId, UUID toUserId) {

        // TODO: send message in socket

        Thread.ofVirtual().name("#ChatThread-", 0).start(() -> {
            save(message, userId, toUserId);
        });
    }


    public void save(String message, UUID userId, UUID toUserId) {

        if (!StringUtils.hasText(message))
            return;

        Chat chat = new Chat();

        Optional<User> user = userService.findById(userId);
        Optional<User> toUser = userService.findById(toUserId);

        if (user.isEmpty() || toUser.isEmpty())
            return;

        chat.setUser(user.get());
        chat.setToUser(toUser.get());
        chat.setMessage(message);
        chat.setSendTime(LocalDateTime.now());

        chatRepo.save(chat);
    }

    public List<Chat> findAllChat(UUID userId, UUID toUserId) {
        Optional<User> user = userService.findById(userId);
        Optional<User> toUser = userService.findById(toUserId);
        if (user.isEmpty() || toUser.isEmpty())
            return Collections.emptyList();

        return chatRepo.findAllByUserAndToUser(user.get(), toUser.get());
    }
}
