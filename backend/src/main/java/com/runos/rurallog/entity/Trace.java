package com.runos.rurallog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data @TableName("t_trace")
public class Trace {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long waybillId;
    private Long stationId;
    private String action;
    private String description;
    private Long operatorId;
    private Date createdAt;
}
