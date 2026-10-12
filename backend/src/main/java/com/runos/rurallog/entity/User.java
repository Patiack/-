package com.runos.rurallog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data @TableName("t_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String phone;
    /** SENDER / VILLAGER / STATION_MASTER / OPS */
    private String role;
    private Long villageId;
}
