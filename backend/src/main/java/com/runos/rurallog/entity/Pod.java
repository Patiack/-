package com.runos.rurallog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/** 签收回执：照片/视频回传给城里子女的核心载体 */
@Data @TableName("t_pod")
public class Pod {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long waybillId;
    /** JSON 数组字符串: ["url1","url2"] */
    private String photos;
    private String videoUrl;
    private String signedName;
    private String relayText;
    private Date createdAt;
}
