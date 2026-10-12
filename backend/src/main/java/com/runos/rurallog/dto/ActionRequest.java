package com.runos.rurallog.dto;

import lombok.Data;

/** 站点操作类请求：入库扫描 / 班线装车发运 */
@Data
public class ActionRequest {
    private String waybillNo;
    private Long stationId;
    private Long routeId;
    private Long operatorId;
}
