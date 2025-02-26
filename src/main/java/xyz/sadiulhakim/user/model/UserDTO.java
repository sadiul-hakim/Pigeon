package xyz.sadiulhakim.user.model;

import lombok.*;

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

    public UserDTO(UUID id, String firstname, String lastname, String email, String picture) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.picture = picture;
    }
}
