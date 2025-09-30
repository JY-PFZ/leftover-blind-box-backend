package nus.iss.se.magicbag.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderQueryDto {
    private Integer pageNum;
    private Integer pageSize;
    private String status;
    private String startDate;
    private String endDate;
    private Long userId;
    private Long merchantId;
}
