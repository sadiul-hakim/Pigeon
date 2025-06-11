package xyz.sadiulhakim.chat.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private long id;
    private String message;
    private String user;
    private String userPicture;
    private String userName;
    private String userTextColor;
    private String toUser;
    private String sendTime;
    private String fileName;
    private String fileContent;
}
