package xyz.sadiulhakim.chat.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.sadiulhakim.user.model.UserDTO;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatSetup {

    private UserDTO user;
    private List<UserDTO> connections;
    private UserDTO selectedUser;
    private long notifications;
}
