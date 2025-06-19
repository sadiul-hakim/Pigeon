package xyz.sadiulhakim.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.sadiulhakim.group.GroupMember;
import xyz.sadiulhakim.group.GroupMemberId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {

    // Find all members of a specific group
    List<GroupMember> findByGroupId(UUID groupId);

    // Find all groups a user belongs to
    List<GroupMember> findByUserId(UUID userId);

    // Find specific membership by group and user IDs
    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    // Check if user is member of group
    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);
}
