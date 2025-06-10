package xyz.sadiulhakim.user.pojo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordDTO {

    @Size(max = 16, min = 5)
    @NotBlank
    private String currentPassword;

    @Size(max = 16, min = 5)
    @NotBlank
    private String newPassword;

    @Size(max = 16, min = 5)
    @NotBlank
    private String confirmPassword;
}
