package nus.iss.se.magicbag.entity;

import com.baomidou.mybatisplus.annotation.TableField; // 🔴 导入注解
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("merchants")
public class Merchant {
    @TableId
    private Integer id;
    // 🟢 新增 user_id 字段，用于关联 users 表
    private Integer userId;
    private String name;
    private Long userId;
    private String phone;
    // 🔴 修复: 添加注解，告诉 MyBatis-Plus 这个字段不映射到数据库表
    @TableField(exist = false)
    private String passwordHash;
    private String businessLicense;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    private double score;
    private Date createdAt;
    private Date updatedAt;
    private Date approvedAt;
}
