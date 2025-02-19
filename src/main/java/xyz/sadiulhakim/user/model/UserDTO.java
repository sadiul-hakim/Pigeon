package xyz.sadiulhakim.user.model;

import lombok.*;
import org.springframework.modulith.NamedInterface;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@NamedInterface("user-dto")
public class UserDTO {
    private UUID id;
    private String firstname;
    private String lastname;
    private String email;
    private String picture;
    private String rawPassword;
}
