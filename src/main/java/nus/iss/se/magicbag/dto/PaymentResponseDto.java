package nus.iss.se.magicbag.dto;

import lombok.Data;

@Data
public class PaymentResponseDto {
    private boolean success;
    private String checkoutUrl; // Stripe URL
    private String message;     // 错误信息
}
