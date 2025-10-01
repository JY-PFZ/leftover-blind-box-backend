package nus.iss.se.magicbag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户信息更新请求")
public class UserUpdateDto {
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Integer id;
    
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    @Schema(description = "用户昵称", example = "张三")
    private String nickname;
    
    @Pattern(regexp = "^https?://.*", message = "头像URL格式不正确")
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
    
    @Schema(description = "头像文件（上传时使用）", hidden = true)
    private transient Object avatarFile;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "8074 1235")
    private String phone;
}
