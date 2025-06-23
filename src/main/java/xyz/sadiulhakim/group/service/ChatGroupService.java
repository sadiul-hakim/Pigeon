package xyz.sadiulhakim.group.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.sadiulhakim.group.ChatGroup;
import xyz.sadiulhakim.group.GroupMember;
import xyz.sadiulhakim.group.GroupMemberId;
import xyz.sadiulhakim.group.enumeration.GroupMemberRole;
import xyz.sadiulhakim.group.event.GroupEvent;
import xyz.sadiulhakim.group.repository.ChatGroupRepository;
import xyz.sadiulhakim.group.repository.GroupMemberRepository;
import xyz.sadiulhakim.user.User;
import xyz.sadiulhakim.user.UserService;
import xyz.sadiulhakim.util.AppProperties;
import xyz.sadiulhakim.util.SecureTextGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class ChatGroupService {

    private final ChatGroupRepository chatGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserService userService;
    private final AppProperties appProperties;
    private final ApplicationEventPublisher eventPublisher;

    public void create(String name) {

        if (!StringUtils.hasText(name)) {
            return;
        }

        // Check users validity
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        if (user == null) {
            return;
        }

        // Create the Group first.
        ChatGroup chatGroup = new ChatGroup();
        chatGroup.setName(name);
        chatGroup.setCreatedAt(LocalDateTime.now());
        chatGroup.setPicture(appProperties.getGroupImageName());

        String channelName = SecureTextGenerator.generateRandomText(10) + "_" +
                name.trim().toLowerCase().replace(" ", "_");
        chatGroup.setChannel(channelName);

        ChatGroup group = chatGroupRepository.save(chatGroup);

        // Now create a GroupMember for this user and add to the group as OWNER
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(user);
        groupMember.setJoinedAt(LocalDateTime.now());
        groupMember.setRole(GroupMemberRole.OWNER);
        groupMember.setId(new GroupMemberId(group.getId(), user.getId()));
        GroupMember member = groupMemberRepository.save(groupMember);

        // Set the member to the group
        group.addMember(member);
        chatGroupRepository.save(group);

        String message = "You created a group names " + name;
        eventPublisher.publishEvent(new GroupEvent(message, user.getId()));
    }

    public String addToGroup(UUID groupId, UUID candidateId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return "Unauthenticated";
        }

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            // Run I/O tasks in parallel
            Future<User> currentUserFuture = executor.submit(() -> userService.findByEmail(authentication.getName()));
            Future<Optional<ChatGroup>> groupFuture = executor.submit(() -> chatGroupRepository.findById(groupId));
            Future<Optional<User>> candidateFuture = executor.submit(() -> userService.findById(candidateId));

            User currentUser = currentUserFuture.get();
            Optional<ChatGroup> groupOpt = groupFuture.get();
            Optional<User> candidateOpt = candidateFuture.get();

            if (currentUser == null) {
                return "Unauthenticated";
            }

            if (groupOpt.isEmpty()) {
                return "Could not find the group!";
            }

            if (candidateOpt.isEmpty()) {
                return "Could not find the candidate!";
            }

            ChatGroup group = groupOpt.get();
            User candidate = candidateOpt.get();

            // Check current user's membership
            Optional<GroupMember> currentMembershipOpt =
                    groupMemberRepository.findByGroupIdAndUserId(groupId, currentUser.getId());

            if (currentMembershipOpt.isEmpty()) {
                return "You are not a member of this group!";
            }

            GroupMember currentMembership = currentMembershipOpt.get();
            GroupMemberRole role = currentMembership.getRole();

            if (role != GroupMemberRole.OWNER && role != GroupMemberRole.ADMIN) {
                return "You are not allowed to add anyone to the group!";
            }

            // Create and save new group member
            GroupMember newMember = new GroupMember();
            newMember.setId(new GroupMemberId(groupId, candidateId));
            newMember.setUser(candidate);
            newMember.setGroup(group);
            newMember.setAddedBy(currentUser);
            newMember.setRole(GroupMemberRole.MEMBER);
            newMember.setJoinedAt(LocalDateTime.now());

            groupMemberRepository.save(newMember);

            executor.submit(() -> {
                String message = currentUser.getFirstname() + " " + currentUser.getLastname() + " has added you to group " + group.getName();
                eventPublisher.publishEvent(new GroupEvent(message, candidateId));
            });

            return "Successfully added to the group.";
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return "Failed to add member due to internal error.";
        }
    }

    public String removeFromGroup(UUID groupId, UUID candidateId) {

        // 1. Authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName()) || candidateId == null) {
            return "Unauthenticated";
        }

        // 2. Parallel load user and group
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<User> userFuture = executor.submit(() -> userService.findByEmail(authentication.getName()));
            Future<Optional<ChatGroup>> groupFuture = executor.submit(() -> chatGroupRepository.findById(groupId));

            User currentUser = userFuture.get();
            Optional<ChatGroup> chatGroupOpt = groupFuture.get();

            if (currentUser == null) {
                return "Unauthenticated";
            }

            if (currentUser.getId().equals(candidateId)) {
                return "You can not remove yourself";
            }

            if (chatGroupOpt.isEmpty()) {
                return "Could not find the group!";
            }

            ChatGroup group = chatGroupOpt.get();

            // 3. Fetch both memberships in parallel
            Future<Optional<GroupMember>> currentMembershipFuture = executor.submit(() ->
                    groupMemberRepository.findByGroupIdAndUserId(groupId, currentUser.getId()));
            Future<Optional<GroupMember>> targetMembershipFuture = executor.submit(() ->
                    groupMemberRepository.findByGroupIdAndUserId(groupId, candidateId));

            Optional<GroupMember> currentMembershipOpt = currentMembershipFuture.get();
            Optional<GroupMember> targetMembershipOpt = targetMembershipFuture.get();

            if (currentMembershipOpt.isEmpty()) {
                return "You are not a member of this group!";
            }

            GroupMember currentMembership = currentMembershipOpt.get();
            if (currentMembership.getRole() != GroupMemberRole.OWNER &&
                    currentMembership.getRole() != GroupMemberRole.ADMIN) {
                return "You are not allowed to remove anyone from the group!";
            }

            if (targetMembershipOpt.isEmpty()) {
                return "Could not find the member!";
            }

            GroupMember targetMembership = targetMembershipOpt.get();

            // 4. Remove the member from the group
            group.getMembers().removeIf(member -> member.getId().equals(targetMembership.getId()));
            chatGroupRepository.save(group);

            // 5. Delete orphaned membership
            executor.submit(() -> groupMemberRepository.delete(targetMembership));

            executor.submit(() -> {
                String message = currentUser.getFirstname() + " " + currentUser.getLastname() + " has removed you from group " + group.getName();
                eventPublisher.publishEvent(new GroupEvent(message, candidateId));
            });

            return "Successfully removed from the group.";
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return "Failed to remove member due to internal error.";
        }
    }


    public String leaveGroup(UUID groupId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !StringUtils.hasText(auth.getName())) {
            return "Unauthenticated";
        }

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            // Parallel fetch user, group, membership
            Future<User> userFuture = executor.submit(() -> userService.findByEmail(auth.getName()));
            Future<Optional<ChatGroup>> groupFuture = executor.submit(() -> chatGroupRepository.findById(groupId));
            User user = userFuture.get();

            if (user == null) {
                return "Unauthenticated";
            }

            Optional<ChatGroup> groupOpt = groupFuture.get();
            if (groupOpt.isEmpty()) {
                return "Could not find the group!";
            }

            Future<Optional<GroupMember>> memberFuture = executor.submit(() ->
                    groupMemberRepository.findByGroupIdAndUserId(groupId, user.getId()));
            Optional<GroupMember> memberOpt = memberFuture.get();

            if (memberOpt.isEmpty()) {
                return "You are not a member of this group!";
            }

            GroupMember groupMember = memberOpt.get();

            if (groupMember.getRole() == GroupMemberRole.OWNER) {
                return "You can not leave this group. You can close this group!";
            }

            // Remove the member from group
            ChatGroup group = groupOpt.get();
            group.getMembers().removeIf(member -> member.getId().equals(groupMember.getId()));
            chatGroupRepository.save(group);

            // Delete the group membership directly
            executor.submit(() -> groupMemberRepository.delete(groupMember));

            // Publish event after successful leave
            executor.submit(() -> {
                String msg = "You successfully left the group " + groupOpt.get().getName();
                eventPublisher.publishEvent(new GroupEvent(msg, user.getId()));
            });

            return "Successfully left the group.";

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return "Failed to leave the group due to an internal error.";
        }
    }

    public String closeGroup(UUID groupId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return "Unauthenticated";
        }

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<User> userFuture = executor.submit(() -> userService.findByEmail(authentication.getName()));
            Future<Optional<ChatGroup>> groupFuture = executor.submit(() -> chatGroupRepository.findById(groupId));
            User user = userFuture.get();
            Optional<ChatGroup> groupOpt = groupFuture.get();

            if (user == null) {
                return "Unauthenticated";
            }

            if (groupOpt.isEmpty()) {
                return "Could not find the group!";
            }

            ChatGroup group = groupOpt.get();

            // Only OWNER can close
            Optional<GroupMember> memberOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, user.getId());
            if (memberOpt.isEmpty()) {
                return "You are not a member of this group!";
            }

            GroupMember member = memberOpt.get();
            if (member.getRole() != GroupMemberRole.OWNER) {
                return "You are not allowed to close this group!";
            }

            // --- Perform Cleanup ---
            // TODO: Delete all chat messages and files for the group
            // Example: chatMessageRepository.deleteAllByGroupId(groupId);

            List<GroupMember> members = group.getMembers();
            for (GroupMember groupMember : members) {

                // Publish closure event
                executor.submit(() -> {
                    String msg = "Owner closed the group " + group.getName();
                    eventPublisher.publishEvent(new GroupEvent(msg, groupMember.getUser().getId()));
                });
            }

            // Remove members from group
            group.setMembers(new ArrayList<>());
            chatGroupRepository.save(group);

            // Delete members and group parallel.
            Future<?> deleteMembersFuture = executor.submit(() -> groupMemberRepository.deleteAll(members));
            Future<?> deleteGroupFuture = executor.submit(() -> chatGroupRepository.delete(group));

            deleteMembersFuture.get(); // Wait & catch exceptions
            deleteGroupFuture.get();

            // Publish closure event
            executor.submit(() -> {
                String msg = "You successfully closed the group " + group.getName();
                eventPublisher.publishEvent(new GroupEvent(msg, user.getId()));
            });

            return "Successfully closed the group.";
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return "Failed to close the group due to an internal error.";
        }
    }

    public List<ChatGroup> joinedGroups(UUID user) {
        List<GroupMember> memberships = groupMemberRepository.findByUserId(user);
        return memberships.stream().map(GroupMember::getGroup).toList();
    }

    public String makeOrRemoveAdmin(UUID groupId, UUID candidateId, boolean make) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return "Unauthenticated";
        }

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            // 1. Parallel fetch: current user, group, candidate
            Future<User> currentUserFuture = executor.submit(() -> userService.findByEmail(authentication.getName()));
            Future<Optional<ChatGroup>> groupFuture = executor.submit(() -> chatGroupRepository.findById(groupId));
            Future<Optional<User>> candidateFuture = executor.submit(() -> userService.findById(candidateId));

            User currentUser = currentUserFuture.get();
            Optional<ChatGroup> groupOpt = groupFuture.get();
            Optional<User> candidateOpt = candidateFuture.get();

            if (currentUser == null) return "Unauthenticated";
            if (groupOpt.isEmpty()) return "Group not found!";
            if (candidateOpt.isEmpty()) return "Candidate not found!";

            User candidate = candidateOpt.get();

            // 2. Validate permissions and memberships
            GroupMember currentMember = groupMemberRepository
                    .findByGroupIdAndUserId(groupId, currentUser.getId())
                    .orElse(null);
            if (currentMember == null) return "You are not a member of this group!";
            if (currentMember.getRole() != GroupMemberRole.OWNER)
                return "Only the group owner can change admin status.";

            GroupMember candidateMember = groupMemberRepository
                    .findByGroupIdAndUserId(groupId, candidate.getId())
                    .orElse(null);
            if (candidateMember == null) return "The candidate is not in this group!";

            // 3. Apply role change
            candidateMember.setRole(make ? GroupMemberRole.ADMIN : GroupMemberRole.MEMBER);
            groupMemberRepository.save(candidateMember);

            ChatGroup chatGroup = groupOpt.get();

            // Publish closure event
            executor.submit(() -> {
                String msg = "You have been promoted to ADMIN in group " + chatGroup.getName();
                eventPublisher.publishEvent(new GroupEvent(msg, candidateId));
            });

            return make
                    ? "Successfully promoted member to admin!"
                    : "Successfully removed member from admin role!";
        } catch (Exception ex) {
            Thread.currentThread().interrupt(); // Preserve interrupt status
            return "Internal error occurred while modifying admin status.";
        }
    }
}
