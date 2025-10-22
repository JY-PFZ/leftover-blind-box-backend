package nus.iss.se.magicbag.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalTime;
import java.util.Date;

@Data
public class MagicBagCreateDto {
    
    @NotNull(message = "商家ID不能为空")
    private Integer merchantId;
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    private String title;
    
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
    
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private Float price;
    
    @NotNull(message = "库存数量不能为空")
    @Min(value = 1, message = "库存数量必须大于0")
    private Integer quantity;
    
    @NotNull(message = "自提开始时间不能为空")
    private LocalTime pickupStartTime;
    
    @NotNull(message = "自提结束时间不能为空")
    private LocalTime pickupEndTime;
    
    @NotNull(message = "有效日期不能为空")
    private Date availableDate;
    
    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;
    
    private String imageUrl;
    
    @AssertTrue(message = "自提结束时间必须晚于开始时间")
    public boolean isValidPickupTime() {
        if (pickupStartTime == null || pickupEndTime == null) {
            return true; // 让@NotNull处理空值情况
        }
        return pickupEndTime.isAfter(pickupStartTime);
    }
}