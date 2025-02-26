package xyz.sadiulhakim.chat.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.sadiulhakim.chat.model.Chat;
import xyz.sadiulhakim.user.model.UserDTO;

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
}
