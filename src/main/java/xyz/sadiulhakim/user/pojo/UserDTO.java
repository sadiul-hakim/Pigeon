package xyz.sadiulhakim.user.pojo;

import lombok.*;
import xyz.sadiulhakim.user.enumeration.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDTO {
    private UUID id;
    private String firstname;
    private String lastname;
    private String email;
    private String picture;
    private String rawPassword;
    private String textColor;
    private LocalDateTime lastSeen;
    private UserStatus status;

    public UserDTO(UUID id, String firstname, String lastname, String email, String picture, String textColor,
                   LocalDateTime lastSeen, UserStatus status) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.picture = picture;
        this.textColor = textColor;
        this.lastSeen = lastSeen;
        this.status = status;
    }
}
