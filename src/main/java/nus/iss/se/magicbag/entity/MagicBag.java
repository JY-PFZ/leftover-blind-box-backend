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
    private Integer merchant_id;
    private String title;
    private String description;
    private float price;
    private Integer quantity;
    private LocalTime pickup_start_time;
    private LocalTime pickup_end_time;
    private Date available_date;
    private String category;
    private String image_url;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
