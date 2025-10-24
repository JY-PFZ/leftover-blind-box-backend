package nus.iss.se.magicbag.dto;

import lombok.Data;

@Data
public class TaskQo {
    private Integer id;
    private Integer type;
    private Integer status;
    private Long applicant;
    private Integer pageNum;
    private Integer pageSize;
}
