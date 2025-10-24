package nus.iss.se.magicbag.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskType {
    MERCHANT_APPROVAL(1, "商家注册");

    private final Integer code;
    private final String desc;

    /**
     * 获取所有状态码列表（用于前端下拉选项等）
     */
    public static TaskType[] all() {
        return values();
    }
}
