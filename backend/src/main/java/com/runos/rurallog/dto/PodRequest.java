package com.runos.rurallog.dto;

import lombok.Data;

import java.util.List;

/** 签收拍照回传请求 */
@Data
public class PodRequest {
    private List<String> photos;
    private String videoUrl;
    private String signedName;
    /** 语音转述文本 */
    private String relayText;
}
