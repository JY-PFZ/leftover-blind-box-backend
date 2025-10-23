package nus.iss.se.magicbag.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemDto {
    private Integer id;
    private Integer orderId;
    private Integer magicBagId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    // 可以添加关联的商品信息，方便前端展示
    private String magicBagTitle;
    private String magicBagImageUrl;
    private String magicBagCategory;
}
