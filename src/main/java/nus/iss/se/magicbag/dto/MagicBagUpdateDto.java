package nus.iss.se.magicbag.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalTime;
import java.util.Date;

@Data
public class MagicBagUpdateDto {
    
    @Size(max = 100, message = "标题长度不能超过100个字符")
    private String title;
    
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
    
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private Float price;
    
    @Min(value = 0, message = "库存数量不能为负数")
    private Integer quantity;
    
    private LocalTime pickupStartTime;
    
    private LocalTime pickupEndTime;
    
    private Date availableDate;
    
    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;
    
    private String imageUrl;
    
    private Boolean isActive;
    
    @AssertTrue(message = "自提结束时间必须晚于开始时间")
    public boolean isValidPickupTime() {
        if (pickupStartTime == null || pickupEndTime == null) {
            return true; // 允许部分更新
        }
        return pickupEndTime.isAfter(pickupStartTime);
    }
}
