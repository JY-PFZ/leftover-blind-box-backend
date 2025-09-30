package nus.iss.se.magicbag.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("merchants")
public class Merchants {
    @TableId
    private Long id;
    private String name;
    private String phone;
    private String passwordHash;
    private String businessLicense;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private Date approvedAt;
}
