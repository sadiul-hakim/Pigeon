package xyz.sadiulhakim.chat.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.sadiulhakim.chat.Chat;
import xyz.sadiulhakim.chat.enumeration.ChatArea;
import xyz.sadiulhakim.group.ChatGroup;
import xyz.sadiulhakim.group.GroupMember;
import xyz.sadiulhakim.user.pojo.UserDTO;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatSetup {

    private UserDTO user;
    private List<UserDTO> connections = new ArrayList<>();
    private UserDTO selectedUser;
    private List<Chat> initialChat = new ArrayList<>();
    private long notifications;
    private List<ChatGroup> groups;
    private ChatArea area;
    private ChatGroup selectedGroup;
    private GroupMember userMembershipInSelectedGroup;
}
