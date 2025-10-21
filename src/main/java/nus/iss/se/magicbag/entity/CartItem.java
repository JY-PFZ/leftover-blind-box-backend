package nus.iss.se.magicbag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Represents an item within a shopping cart.
 * This entity maps to the 'cart_items' table in the database.
 */
@Data
@TableName("cart_items")
public class CartItem {

    /**
     * 🔴 最终修复:
     * 根据数据库 schema, 主键列的真实名称是 "item_id"。
     * 此注解将确保 MyBatis-Plus 生成正确的 SQL,
     * 例如: DELETE FROM cart_items WHERE item_id=?
     */
    @TableId(value = "item_id", type = IdType.AUTO)
    private Integer cartItemId;

    private Integer cartId;

    private Integer magicBagId;

    private int quantity;

    private LocalDateTime addedAt;

    /**
     * 🟢 新增字段:
     * 与数据库中的 'status' 列对应.
     */
    private String status;
}

