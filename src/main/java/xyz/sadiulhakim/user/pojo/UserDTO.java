package xyz.sadiulhakim.user.pojo;

import lombok.*;

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

    public UserDTO(UUID id, String firstname, String lastname, String email, String picture, String textColor,
                   LocalDateTime lastSeen) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.picture = picture;
        this.textColor = textColor;
        this.lastSeen = lastSeen;
    }
}
