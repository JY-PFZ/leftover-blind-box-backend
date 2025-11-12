package nus.iss.se.magicbag.dto.event;


import java.util.Date;

public record MerchantProcessedEvent(
    Long userId,
    Integer status,
    Long operatorId,
    Date endAt,
    String reason // 拒绝理由
) {

}
