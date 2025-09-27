package nus.iss.se.magicbag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDto {
    private Integer id;

    @NotBlank(message = "Username is empty")
    private String username;
    private String role;
    private String phone;
    private String nickname;
    private String avatar;
}
