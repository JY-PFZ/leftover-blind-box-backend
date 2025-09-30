package nus.iss.se.magicbag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CartDto {
    private Integer cartId;   
    private Integer userId;   
    private List<CartItemDto> items; 
    private double total;          
}
