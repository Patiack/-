package com.runos.rurallog.dto;

import com.runos.rurallog.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 下单/上行收件 通用请求体 */
@Data
public class CreateOrderRequest {
    /** SEND / AGENT / AGRI */
    private String type;
    private Long userId;
    private String senderName;
    private String senderPhone;
    private String receiverName;
    private String receiverPhone;
    /** 下行=目的村站；上行=收件村站 */
    private Long receiverStationId;
    private String itemDesc;
    private BigDecimal weight;
    private BigDecimal goodsAmount;
    private BigDecimal freight;
    private String remark;
    private List<OrderItem> items;
}
