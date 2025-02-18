package xyz.sadiulhakim.user.model;

import lombok.*;
import org.springframework.modulith.NamedInterface;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@NamedInterface("user-dto")
public class UserDTO {
    private long id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String role;
    private String picture;
    private String textColor;
    private LocalDateTime createdAt;
}
