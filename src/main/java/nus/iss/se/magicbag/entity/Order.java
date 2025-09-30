package nus.iss.se.magicbag.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("orders")
public class Order {
    @TableId
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
    private Date updatedAt;
    private Date paidAt;
    private Date completedAt;
    private Date cancelledAt;
}
