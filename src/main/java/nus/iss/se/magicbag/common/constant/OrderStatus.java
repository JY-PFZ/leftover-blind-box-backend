package nus.iss.se.magicbag.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    PAID("paid", "已支付"),
    COMPLETED("completed","完成"),
    CANCELLED("cancelled","已取消");

    private final String code;
    private final String desc;

    /**
     * 获取所有状态码列表（用于前端下拉选项等）
     */
    public static OrderStatus[] all() {
        return values();
    }

    public static Optional<OrderStatus> getByCode(String code) {
        return Arrays.stream(values())
                .filter(role -> role.code.equalsIgnoreCase(code.toUpperCase()))
                .findFirst();
    }
}
