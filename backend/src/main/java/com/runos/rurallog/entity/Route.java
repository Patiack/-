package com.runos.rurallog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data @TableName("t_route")
public class Route {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    /** 站点 id 链, 逗号分隔: 1,2,4,5 */
    private String stations;
}
