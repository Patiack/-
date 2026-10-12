package com.runos.rurallog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data @TableName("t_waybill")
public class Waybill {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String waybillNo;
    private Long orderId;
    private Long routeId;
    private Long currentStationId;
    private String status;
}
