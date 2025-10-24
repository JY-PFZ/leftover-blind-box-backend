package nus.iss.se.magicbag.dto.event;

import java.math.BigDecimal;

public record MerchantRegisterEvent(
        Long userId,
        Long merchantId,
        String shopName,
        String phone,
        String address,
        // 餐馆图片URL
        String businessLicense,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
