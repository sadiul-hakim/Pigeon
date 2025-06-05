package xyz.sadiulhakim.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import xyz.sadiulhakim.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRepo extends JpaRepository<Chat, Long> {

    List<Chat> findAllByUserAndToUserOrToUserAndUserOrderBySendTime(User user, User toUser, User toUser2, User user2);

    List<Chat> findAllBySendTimeBetween(LocalDateTime start, LocalDateTime end);

    @Modifying
    @Transactional
    @Query("DELETE FROM Chat c WHERE (c.user.email = :user AND c.toUser.email = :toUser) OR (c.user.email = :toUser AND c.toUser.email = :user)")
    void deleteChatBetweenUsers(@Param("user") String user, @Param("toUser") String toUser);

}
