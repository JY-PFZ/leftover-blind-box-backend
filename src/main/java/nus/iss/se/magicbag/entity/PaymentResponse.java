package nus.iss.se.magicbag.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private boolean success;
    private String redirectUrl; // Stripe 支付页面 URL
    private String message;
}
