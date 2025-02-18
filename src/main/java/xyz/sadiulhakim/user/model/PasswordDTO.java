package xyz.sadiulhakim.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordDTO {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
