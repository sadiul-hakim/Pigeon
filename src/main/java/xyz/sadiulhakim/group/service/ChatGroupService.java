package xyz.sadiulhakim.group.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.sadiulhakim.group.ChatGroup;
import xyz.sadiulhakim.group.GroupMember;
import xyz.sadiulhakim.group.GroupMemberId;
import xyz.sadiulhakim.group.enumeration.GroupMemberRole;
import xyz.sadiulhakim.group.repository.ChatGroupRepository;
import xyz.sadiulhakim.group.repository.GroupMemberRepository;
import xyz.sadiulhakim.user.User;
import xyz.sadiulhakim.user.UserService;
import xyz.sadiulhakim.util.AppProperties;
import xyz.sadiulhakim.util.FileUtil;
import xyz.sadiulhakim.util.SecureTextGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatGroupService {

    private final ChatGroupRepository chatGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserService userService;
    private final AppProperties appProperties;

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
    }

    public String addToGroup(UUID groupId, UUID candidate) {

        // Check users validity
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        if (user == null) {
            return "Unauthenticated";
        }

        // Check group validity
        Optional<ChatGroup> chatGroup = chatGroupRepository.findById(groupId);
        if (chatGroup.isEmpty()) {
            return "Could not find the group!";
        }

        // Check candidate validity
        Optional<User> candidateOpt = userService.findById(candidate);
        if (candidateOpt.isEmpty()) {
            return "Could not find the candidate!";
        }

        // Check member validity
        Optional<GroupMember> addingPersonOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, user.getId());
        if (addingPersonOpt.isEmpty()) {
            return "You are not a member of this group!";
        }

        // Check users role
        GroupMember addingPerson = addingPersonOpt.get();
        if (!(addingPerson.getRole().equals(GroupMemberRole.OWNER) ||
                addingPerson.getRole().equals(GroupMemberRole.ADMIN))) {
            return "You are not allowed to add anyone to the group!";
        }

        GroupMember groupMember = new GroupMember();
        groupMember.setUser(candidateOpt.get());
        groupMember.setAddedBy(user);
        groupMember.setGroup(chatGroup.get());
        groupMember.setRole(GroupMemberRole.MEMBER);
        groupMember.setJoinedAt(LocalDateTime.now());
        groupMemberRepository.save(groupMember);

        return "Successfully added to the group.";
    }

    public String removeFromGroup(UUID groupId, UUID candidate) {

        // Check users validity
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        if (user == null) {
            return "Unauthenticated";
        }

        // Check group validity
        Optional<ChatGroup> chatGroup = chatGroupRepository.findById(groupId);
        if (chatGroup.isEmpty()) {
            return "Could not find the group!";
        }

        // Check member validity
        Optional<GroupMember> removingPersonOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, user.getId());
        if (removingPersonOpt.isEmpty()) {
            return "You are not a member of this group!";
        }

        // Check users role
        GroupMember addingPerson = removingPersonOpt.get();
        if (!(addingPerson.getRole().equals(GroupMemberRole.OWNER) ||
                addingPerson.getRole().equals(GroupMemberRole.ADMIN))) {
            return "You are not allowed to remove anyone from the group!";
        }

        Optional<GroupMember> member = groupMemberRepository.findByGroupIdAndUserId(groupId, candidate);
        if (member.isEmpty()) {
            return "Could not find the member!";
        }

        GroupMember groupMember = member.get();
        ChatGroup group = chatGroup.get();

        // remove from group
        group.getMembers().remove(groupMember);
        chatGroupRepository.save(group);

        // As this user is not in this group anymore. There is no need of this membership.
        groupMemberRepository.delete(groupMember);
        return "Successfully removed from the group.";
    }

    public String leaveGroup(UUID groupId) {

        // Check users validity
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        if (user == null) {
            return "Unauthenticated";
        }

        // Check group validity
        Optional<ChatGroup> chatGroup = chatGroupRepository.findById(groupId);
        if (chatGroup.isEmpty()) {
            return "Could not find the group!";
        }

        Optional<GroupMember> member = groupMemberRepository.findByGroupIdAndUserId(groupId, user.getId());
        if (member.isEmpty()) {
            return "Could not find the member!";
        }

        GroupMember groupMember = member.get();
        ChatGroup group = chatGroup.get();

        // remove from group
        group.getMembers().remove(groupMember);
        chatGroupRepository.save(group);

        // As this user is not in this group anymore. There is no need of this membership.
        groupMemberRepository.delete(groupMember);
        return "Successfully left the group.";
    }

    public List<ChatGroup> joinedGroups(UUID user) {
        List<GroupMember> memberships = groupMemberRepository.findByUserId(user);
        return memberships.stream().map(GroupMember::getGroup).toList();
    }
}
