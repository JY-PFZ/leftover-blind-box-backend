package nus.iss.se.magicbag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Schema(description = "Login request parameters")
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Integer id;

    @NotBlank(message = "Username is empty")
    @Schema(description = "username,should not be null", example = "123@gmail.com")
    private String username;
    private String role;
    private String phone;
    private String nickname;
    private String avatar;
}
