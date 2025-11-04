package nus.iss.se.magicbag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("admin_task")
public class AdminTask {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private Integer type;
    private Integer status;
    private Long applicant;
    private Long operator;
    private String data;
    private Date startTime;
    private Date endTime;
    private String comment;
}
