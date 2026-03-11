package com.tyut.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息送达回执DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(description = "消息送达回执DTO")
public class DeliveryAckDTO {

    @ApiModelProperty(value = "会话ID", example = "1")
    private Long sessionId;

    @ApiModelProperty(value = "消息ID", example = "100")
    private Long messageId;
}
