package xyz.sadiulhakim.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.sadiulhakim.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRepo extends JpaRepository<Chat, Long> {

    List<Chat> findAllByUserAndToUserOrToUserAndUserOrderBySendTime(User user, User toUser, User toUser2, User user2);

    List<Chat> findAllBySendTimeBetween(LocalDateTime start, LocalDateTime end);

    @Modifying
    @Query("DELETE FROM Chat c WHERE (c.user = :user AND c.toUser = :toUser) OR (c.user = :toUser AND c.toUser = :user)")
    void deleteChatBetweenUsers(@Param("user") User user, @Param("toUser") User toUser);

}
