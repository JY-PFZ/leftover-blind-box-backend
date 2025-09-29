package nus.iss.se.magicbag.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MerchantDto {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
}
