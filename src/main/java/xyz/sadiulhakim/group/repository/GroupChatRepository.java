package xyz.sadiulhakim.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.sadiulhakim.group.ChatGroup;
import xyz.sadiulhakim.group.GroupChat;

import java.util.List;

public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
    List<GroupChat> findAllByGroup(ChatGroup group);
    void deleteAllByGroup(ChatGroup group);
}
