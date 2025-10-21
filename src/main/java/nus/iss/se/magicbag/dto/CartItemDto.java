package nus.iss.se.magicbag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemDto {
    private Integer itemId;
    private Integer magicbagId; // 🔴 新增字段
    private String bagName;
    private double price;
    private int quantity;
    private double subtotal;
}
