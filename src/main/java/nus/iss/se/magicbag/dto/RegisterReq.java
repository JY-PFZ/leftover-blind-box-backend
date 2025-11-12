package nus.iss.se.magicbag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;

@Data
@Schema(description = "Register request parameters")
public class RegisterReq {
    @NotBlank(message = "username is null")
    @Email(message = "Please enter the correct email")
    @Schema(description = "username", example = "123@gmail.com")
    private String username;

    @NotBlank(message = "password is null")
    @Schema(description = "Password (The front end needs to be encrypted with RSA public key before transmission)", example = "AQIDBAUG...")
    private String password;

    @NotBlank(message = "role is not defined")
    @Schema(description = "role, now we just allow a user having only one role", example = "USER/MERCHANT/ADMIN")
    private String role;
}
