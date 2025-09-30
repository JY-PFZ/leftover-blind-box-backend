package nus.iss.se.magicbag.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderStatsDto {
    private Long totalOrders;
    private BigDecimal totalAmount;
    private Long pendingOrders;
    private Long paidOrders;
    private Long completedOrders;
    private Long cancelledOrders;
}
