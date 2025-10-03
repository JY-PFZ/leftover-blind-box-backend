package nus.iss.se.magicbag.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@Data
@TableName("magic_bags")
public class MagicBag {

    @TableId
    private Integer id;

    private Integer merchantId;
    private String title;
    private String description;
    private float price;
    private Integer quantity;
    private LocalTime pickupStartTime;
    private LocalTime pickupEndTime;
    private Date availableDate;
    private String category;
    private String imageUrl;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
