package nus.iss.se.magicbag.dto;

import lombok.Data;
import java.util.Date;

@Data
public class OrderVerificationDto {
    private Long id;
    private Long orderId;
    private Long verifiedBy;
    private Date verifiedAt;
    private String location;
    private String verifierName;
}
