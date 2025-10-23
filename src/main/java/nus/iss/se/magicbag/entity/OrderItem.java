package nus.iss.se.magicbag.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_items")
public class OrderItem {
    
    @TableId
    private Integer id;
    
    private Integer orderId;
    
    private Integer magicBagId;
    
    private Integer quantity;
    
    private BigDecimal unitPrice;
    
    private BigDecimal subtotal;
    
    private LocalDateTime createdAt;
}
