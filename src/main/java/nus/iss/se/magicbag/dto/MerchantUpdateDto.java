package nus.iss.se.magicbag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "商家信息更新请求")
public class MerchantUpdateDto {
    @NotNull(message = "商家ID不能为空")
    @Schema(description = "商家ID", example = "1")
    private Integer id;
    
    @NotBlank(message = "商家名称不能为空")
    @Size(max = 100, message = "商家名称长度不能超过100个字符")
    @Schema(description = "商家名称", example = "美味餐厅")
    private String name;

    @Pattern(regexp = "^$|^[89]\\d{7}$", message = "手机号格式不正确（新加坡8位手机号，以8或9开头）")
    @Schema(description = "联系手机号（可选）", example = "81234567")
    private String phone;
    
    @NotBlank(message = "地址不能为空")
    @Size(max = 255, message = "地址长度不能超过255个字符")
    @Schema(description = "详细地址", example = "Unit 1, 1000, Bedok North Street 5")
    private String address;
    
    @Schema(description = "餐馆图片URL", example = "https://example.com/restaurant.jpg")
    private String businessLicense;
    
    @Schema(description = "纬度", example = "39.9042")
    private BigDecimal latitude;
    
    @Schema(description = "经度", example = "116.4074")
    private BigDecimal longitude;
    
    @Schema(description = "审核状态", example = "approved")
    private String status;
}