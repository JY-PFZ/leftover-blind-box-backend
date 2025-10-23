package nus.iss.se.magicbag.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItemDto {
    
    private Integer id;
    
    private Integer orderId;
    
    private Integer magicBagId;
    
    private String magicBagTitle;
    
    private String magicBagImageUrl;
    
    private String magicBagCategory;
    
    private Integer quantity;
    
    private BigDecimal unitPrice;
    
    private BigDecimal subtotal;
    
    private LocalDateTime createdAt;
}
