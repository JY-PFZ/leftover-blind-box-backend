package nus.iss.se.magicbag.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderDto {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long bagId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String status;
    private String pickupCode;
    private Date pickupStartTime;
    private Date pickupEndTime;
    private Date createdAt;
    private Date paidAt;
    private Date completedAt;
    private Date cancelledAt;
    
    // 关联信息
    private String userName;
    private String bagTitle;
    private String merchantName;
}
