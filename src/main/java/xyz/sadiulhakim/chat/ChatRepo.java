package xyz.sadiulhakim.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import xyz.sadiulhakim.user.User;

import java.util.List;

public interface ChatRepo extends JpaRepository<Chat, Long> {

    @Query("SELECT c FROM Chat c WHERE (c.user = :user1 AND c.toUser = :user2) OR (c.user = :user2 AND c.toUser = :user1) ORDER BY c.sendTime")
    List<Chat> findConversation(@Param("user1") User user1, @Param("user2") User user2);

    @Modifying
    @Transactional
    @Query("DELETE FROM Chat c WHERE (c.user.email = :user AND c.toUser.email = :toUser) OR (c.user.email = :toUser AND c.toUser.email = :user)")
    void deleteChatBetweenUsers(@Param("user") String user, @Param("toUser") String toUser);
}
