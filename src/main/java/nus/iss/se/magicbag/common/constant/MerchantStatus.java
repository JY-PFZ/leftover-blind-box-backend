package nus.iss.se.magicbag.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MerchantStatus {
    APPROVED("approved", "通过"),
    PENDING("pending","等待审核");

    private final String code;
    private final String desc;

    /**
     * 获取所有状态码列表（用于前端下拉选项等）
     */
    public static MerchantStatus[] all() {
        return values();
    }
}
