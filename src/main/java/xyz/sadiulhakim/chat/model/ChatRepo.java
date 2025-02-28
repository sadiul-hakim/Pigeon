package xyz.sadiulhakim.chat.model;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.sadiulhakim.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRepo extends JpaRepository<Chat, Long> {

    List<Chat> findAllByUserAndToUserOrToUserAndUserOrderBySendTime(User user, User toUser, User toUser2, User user2);

    List<Chat> findAllBySendTimeBetween(LocalDateTime start, LocalDateTime end);

    void deleteAllByUserAndToUserOrToUserAndUser(User user, User toUser, User toUser2, User user2);
}
