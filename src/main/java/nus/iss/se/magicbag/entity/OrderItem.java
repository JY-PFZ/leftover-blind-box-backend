package nus.iss.se.magicbag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime; // 使用 LocalDateTime 替代旧的 Date

@Data
@TableName("order_items")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;
    private Integer magicBagId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private LocalDateTime createdAt; // 建议使用 LocalDateTime

    public OrderItem() {
        this.createdAt = LocalDateTime.now(); // 设置默认创建时间
    }
}
