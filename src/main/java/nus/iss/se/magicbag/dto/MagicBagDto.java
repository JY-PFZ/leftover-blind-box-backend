package nus.iss.se.magicbag.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@Data
public class MagicBagDto {
    private Integer id;
    private Integer merchantId;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private LocalTime pickupStartTime;
    private LocalTime pickupEndTime;
    private Date availableDate;
    private String category;
    private String imageUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
