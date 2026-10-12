package com.runos.rurallog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data @TableName("t_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    /** SEND代寄 / AGENT代购 / AGRI农产品上行 */
    private String type;
    /** DOWN / UP */
    private String direction;
    /** CREATED/PICKED/AT_COUNTY/AT_TOWN/AT_VILLAGE/SIGNED/VILLAGE_RECEIVED/DISPATCHED */
    private String status;
    private Long userId;
    private String senderName;
    private String senderPhone;
    private String receiverName;
    private String receiverPhone;
    private Long receiverStationId;
    private String itemDesc;
    private BigDecimal weight;
    private BigDecimal goodsAmount;
    private BigDecimal freight;
    private String remark;
    private Date createdAt;
    private Date updatedAt;
}
