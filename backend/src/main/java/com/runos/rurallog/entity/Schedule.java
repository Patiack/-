package com.runos.rurallog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data @TableName("t_schedule")
public class Schedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long routeId;
    private String busNo;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date departTime;
    private Integer capacity;
    /** PENDING / RUNNING / FINISHED */
    private String status;
}
