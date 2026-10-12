package com.runos.rurallog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data @TableName("t_station")
public class Station {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    /** COUNTY / TOWN / VILLAGE */
    private String type;
    private Long parentId;
    private String contact;
    private String phone;
    private String address;
}
