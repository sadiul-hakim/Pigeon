package xyz.sadiulhakim.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.sadiulhakim.group.GroupChat;

public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
}
