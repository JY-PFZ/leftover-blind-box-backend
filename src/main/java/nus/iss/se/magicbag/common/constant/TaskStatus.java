package nus.iss.se.magicbag.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStatus {
    PENDING(1, "待处理"),
    PROCESSING(2, "处理中"),
    APPROVED(3, "通过"),
    REJECTED(4, "拒绝");

    private final Integer code;
    private final String desc;

    /**
     * 获取所有状态码列表（用于前端下拉选项等）
     */
    public static TaskStatus[] all() {
        return values();
    }
}
