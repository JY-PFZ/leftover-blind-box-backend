package nus.iss.se.magicbag.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import nus.iss.se.magicbag.common.annotation.RsaDecrypt;

@Getter
public class LoginReq {
    @NotBlank(message = "username is null")
    @Email(message = "Please enter the correct email")
    private String username;

    @RsaDecrypt
    @NotBlank(message = "password is null")
    private String password;

    private String phone;
}
