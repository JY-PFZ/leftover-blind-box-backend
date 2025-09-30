package nus.iss.se.magicbag.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("order_verifications")
public class OrderVerification {
    @TableId
    private Long id;
    private Long orderId;
    private Long verifiedBy;
    private Date verifiedAt;
    private String location;
}
