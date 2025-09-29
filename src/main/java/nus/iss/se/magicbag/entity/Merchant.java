package nus.iss.se.magicbag.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("merchants")
public class Merchant {
    @TableId
    private Long id;
    
    private String name;
    private String phone;
    private String passwordHash;
    private String businessLicense;
    private String address;
    private String latitude;
    private String longitude;
    private String status; // pending, approved, rejected
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;
}
