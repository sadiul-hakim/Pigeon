package xyz.sadiulhakim.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.sadiulhakim.group.ChatGroup;

import java.util.UUID;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, UUID> {
}
