package com.tyut.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话已读同步DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(description = "会话已读同步DTO")
public class MarkSessionReadDTO {

    @ApiModelProperty(value = "会话ID", example = "1")
    private Long sessionId;

    @ApiModelProperty(value = "已读到的最后一条消息ID", example = "100")
    private Long lastReadMessageId;
}
