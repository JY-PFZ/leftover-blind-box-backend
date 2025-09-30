package nus.iss.se.magicbag.dto;

import lombok.Data;
import java.util.Date;

@Data
public class OrderVerificationDto {
    private Integer id;
    private Integer orderId;
    private Integer verifiedBy;
    private Date verifiedAt;
    private String location;
    private String verifierName;
}
