package com.tyut.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送图片消息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(description = "发送图片消息DTO")
public class SendImageMessageDTO {

    @ApiModelProperty(value = "会话ID", example = "1")
    private Long sessionId;

    @ApiModelProperty(value = "图片URL")
    private String imageUrl;
}
