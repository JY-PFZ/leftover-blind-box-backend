package nus.iss.se.magicbag.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("orders")
public class Order {
    @TableId
    private Integer id;
    private String orderNo;
    private Integer userId;
    // 🟡 注意: 对于 'cart' 类型的订单，此字段可能无意义或为 NULL (取决于数据库是否允许)
    private Integer bagId;
    // 🟡 注意: 对于 'cart' 类型的订单，此字段可能代表商品种类数，而不是总件数
    private Integer quantity;
    private BigDecimal totalPrice;
    private String status;
    private String pickupCode;
    // 🟡 注意: 对于 'cart' 类型的订单，可能需要从 order_items 或 merchant 获取
    private Date pickupStartTime;
    // 🟡 注意: 对于 'cart' 类型的订单，可能需要从 order_items 或 merchant 获取
    private Date pickupEndTime;
    private Date createdAt;

    /**
     * 🔴 保持不变: 数据库中没有 updated_at 列
     */
    @TableField(exist = false)
    private Date updatedAt;

    private Date paidAt;
    private Date completedAt;
    private Date cancelledAt;

    /**
     * 🟢 移除注解: 现在数据库中有对应的 order_type 列了
     */
    private String orderType; // "single" 或 "cart"
}

